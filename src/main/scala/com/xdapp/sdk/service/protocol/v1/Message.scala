package com.xdapp.sdk.service.protocol.v1

import scodec.bits._
import scodec.{Codec, DecodeResult}

/**
 * @author alusa on 2019/9/27.
 */
case class Message(prefix: Prefix, header: Header, context: ByteVector, body: ByteVector) {
  def transformToReply(body0: ByteVector): Vector[Message] = {
    val requests = for (body1 <- body0.grouped(0x200000)) yield {
      val flag = if (body1.size == 0x200000) prefix.flag | 2 else prefix.flag | 2 | 4
      val prefix0 =
        prefix.copy(flag = flag, length = Constant.HEADER_LENGTH + context.size + body1.size)
      Message(prefix0, header, context, body1)
    }
    requests.toVector
  }

  def isFinish: Boolean = {
    prefix.checkFlag(Constant.FLAG_SYS_MSG) || prefix.checkFlag(Constant.FLAG_FINISH)
  }
}

object Message {
  implicit val codec: Codec[Message] = Codec(
    request => {
      for {
        bs1 <- Codec.encode(request.prefix)
        bs2 <- Codec.encode(request.header)
      } yield bs1 ++ bs2 ++ request.context.bits ++ request.body.bits
    },
    bits => {
      Codec.decode[Prefix](bits).flatMap {
        case DecodeResult(prefix, remainder1) =>
          Codec.decode[Header](remainder1).map {
            case DecodeResult(header, remainder2) =>
              val offset = prefix.length - Constant.HEADER_LENGTH
              val (remainder3, remainder) = remainder2.bytes.splitAt(offset)
              val (context, body) = remainder3.splitAt(header.contextLength)
              DecodeResult(Message(prefix, header, context, body), remainder.bits)
          }
      }
    }
  )
}