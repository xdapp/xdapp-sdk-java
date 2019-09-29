package com.xdapp.sdk.service

import java.nio.ByteBuffer
import java.util.concurrent.{Executors, ScheduledExecutorService, ScheduledFuture, TimeUnit}

import com.typesafe.scalalogging.LazyLogging
import com.xdapp.sdk.service.model.RemoteServer
import com.xdapp.sdk.service.protocol.v1.Message
import hprose.server.HproseService
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.{ChannelFuture, ChannelHandlerContext, ChannelInitializer, ChannelOption, SimpleChannelInboundHandler}
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import javax.net.ssl.SSLContext
import scodec.bits.{BitVector, ByteVector}
import scodec.{Codec, DecodeResult}

/**
 * @author alusa on 2019/9/29.
 */
abstract class BasicService extends HproseService with LazyLogging {
  val remoteServer: RemoteServer
  val host = remoteServer.host
  val port = remoteServer.port
  val ssl = remoteServer.ssl
  val checkPeriod = remoteServer.checkPeriod
  val currentContext = new ThreadLocal[BasicContext]

  val regErr = new ThreadLocal[Boolean]
  val regSucceed = new ThreadLocal[Boolean]

  def setRegError = {
    regErr.set(true)
  }

  def setRegSucceed = {
    regSucceed.set(true)
  }

  def isRegSucceed = {
    regSucceed.get()
  }

  def isRegError = {
    regErr.get()
  }

  private def defaultHandle(message: Message) = {
    val body = message.body.toByteBuffer
    val context = new BasicContext(message, this)
    currentContext.set(context)
    handle(body, currentContext.get)
  }

  def run(ctx: ChannelHandlerContext, buffer: ByteBuffer): Unit = {
    val message = Codec.decode[Message](BitVector(buffer)).map {
      case DecodeResult(msg, _) =>
        msg
    }.require
    logger.debug(message.toString)
    defaultHandle(message).then((value: ByteBuffer) => {
      if (isRegError) scheduler.shutdown()
      message
        .transformToReply(ByteVector(value))
        .map(m => ctx.writeAndFlush(Unpooled.buffer.writeBytes(Codec.encode(m).require.toByteArray)))
    }).catchError((e: Throwable) => {
      logger.error(e.getMessage)
      e
    })
  }

  val channelFuture: ThreadLocal[ChannelFuture] = new ThreadLocal[ChannelFuture]
  val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

  def start(): Unit = {
    val eventLoopGroup = new NioEventLoopGroup()
    val bootstrap = new Bootstrap
    bootstrap.channel(classOf[NioSocketChannel])
    bootstrap.option[java.lang.Boolean](ChannelOption.SO_KEEPALIVE, true)
    bootstrap.group(eventLoopGroup)
    bootstrap.remoteAddress(host, port)
    bootstrap.handler(new ChannelInitializer[SocketChannel] {
      override def initChannel(ch: SocketChannel): Unit = {
        val pipeline = ch.pipeline()
        if (ssl) {
          val sslContext = SSLContext.getDefault
          val sslEngine = sslContext.createSSLEngine()
          sslEngine.setUseClientMode(true)
          pipeline.addLast("ssl", new io.netty.handler.ssl.SslHandler(sslEngine))
        }
        logger.info(s"connect to $host:$port ...")
        pipeline.addLast(new SimpleChannelInboundHandler[ByteBuf]() {
          override def messageReceived(ctx: ChannelHandlerContext, msg: ByteBuf): Unit = {
            val buffer = msg.nioBuffer
            buffer.rewind
            if (buffer.hasRemaining) run(ctx, buffer)
          }
        })
      }
    })

    scheduler.scheduleAtFixedRate(() => {
      if (channelFuture.get() == null || !channelFuture.get().channel().isOpen) {
        val cf = bootstrap.connect().addListener((future: ChannelFuture) => {
          if (future.isSuccess) {
            logger.info("connect succeed ...")
          } else {
            logger.warn("connect failed, wait for next retry ...")
          }
        })
        channelFuture.set(cf)
      }
    }, 0, checkPeriod, TimeUnit.SECONDS)
  }

  def stop() = {
    scheduler.shutdown()
  }
}
