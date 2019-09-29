package com.xdapp.sdk.service

import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util

import com.typesafe.scalalogging.LazyLogging

import scala.collection.JavaConverters._
import com.xdapp.sdk.service.model.ServiceInfo
import com.xdapp.sdk.service.protocol.Version

/**
 * @author alusa on 2019/9/27.
 */
class SystemService(serviceInfo: ServiceInfo, service: Service) extends LazyLogging {
  val DEFAULT_DOMAIN = "xdapp.com"

  def regError(msg: String): Unit = {
    if (service.currentContext.get == null) return
    service.setRegError
    logger.error(s"[dispatch regError] $msg")
  }

  def regOk(data: util.Map[String, AnyRef], time: Int, rand: String, hash: String): Unit = {
    if (service.currentContext.get == null) return
    if (service.isRegSucceed) return
    if (regHash(time, rand) != hash) {
      logger.error("regOk hash invalid")
      service.setRegError
      return
    }
    service.setRegSucceed
    logger.info("[dispatch regOk]")
  }

  private def regHash(time: Int, rand: String) = {
    val digest = MessageDigest.getInstance("SHA-1")
    digest.update(s"${serviceInfo.app}.${serviceInfo.serviceName}.$time.$rand.${serviceInfo.key}.$DEFAULT_DOMAIN".getBytes(StandardCharsets.UTF_8))
    String.format("%040x", new BigInteger(1, digest.digest))
  }

  def reg(time: Int, rand: String, hash: String): util.Map[String, AnyRef] = {
    logger.info("[dispatch reg]")
    val digest = MessageDigest.getInstance("SHA-1")
    digest.update(s"$time.$rand.$DEFAULT_DOMAIN".getBytes(StandardCharsets.UTF_8))

    if (!(String.format("%040x", new BigInteger(1, digest.digest)) == hash))
      throw new Exception("invalid hash")

    val result = Map[String, AnyRef](
      "status" -> Boolean.box(true),
      "app" -> serviceInfo.app,
      "name" -> serviceInfo.serviceName,
      "time" -> Int.box(time),
      "rand" -> rand,
      "version" -> Version.current,
      "hash" -> regHash(time, rand)
    )

    result.asJava
  }

  def ping(): Boolean = {
    true
  }

  def getFunctions(): util.Collection[String] = {
    if (service.currentContext.get == null) List.empty[String].asJava
    if (!service.isRegSucceed) List.empty[String].asJava
    else service.getGlobalMethods.getAllNames
  }

  def log(typ: String, log: String, data: AnyRef = null): Unit = {
    typ match {
      case "debug" =>
        logger.debug(log, data)
      case "warn" =>
        logger.warn(log, data)
      case _ =>
        logger.info(log, data)
    }
  }

}
