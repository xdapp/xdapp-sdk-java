package com.xdapp.sdk.service

import com.typesafe.scalalogging.LazyLogging
import com.xdapp.sdk.service.model.{RemoteServer, ServiceInfo}
import hprose.server.HproseService

/**
 * @author alusa on 2019/9/27.
 */
class Service(val serviceInfo: ServiceInfo, val remoteServer: RemoteServer) extends BasicService with LazyLogging {

  def addWeb[T](methodName: String, obj: Class[T]): HproseService = {
    getGlobalMethods.addMethod(methodName, obj, s"${serviceInfo.serviceName}_$methodName")
    this
  }

  def addWebScala(methodName: String, obj: Any) = {
    getGlobalMethods.addMethod(methodName, obj, s"${serviceInfo.serviceName}_$methodName")
    this
  }

  def initialize(): HproseService = {
    val sysCall = new SystemService(serviceInfo, this)
    add("reg", sysCall, "sys_reg")
    add("regError", sysCall, "sys_regErr")
    add("regOk", sysCall, "sys_regOk")
    add("ping", sysCall, "sys_ping")
    add("getFunctions", sysCall, "sys_getFunctions")
    add("log", sysCall, "sys_log")
  }

}

object Service {
  private def apply(serviceInfo: ServiceInfo, remoteServer: RemoteServer): Service = {
    val service = new Service(serviceInfo, remoteServer)
    service.initialize()
    service
  }

  val DEV_SERVER = RemoteServer("service-dev.xdapp.com", 8100, true, 60)
  val PROD_SERVER = RemoteServer("service-prod.xdapp.com", 8900, true, 60)
  val GLOBAL_PROD_SERVER = RemoteServer("service-gcp.xdapp.com", 8900, true, 60)

  def connectToLocalDev(serviceInfo: ServiceInfo, host: String = "127.0.0.1", port: Int = 8061): Service = {
    apply(serviceInfo, RemoteServer(host, port, false, 60))
  }

  def connectToDev(serviceInfo: ServiceInfo): Service = {
    apply(serviceInfo, DEV_SERVER)
  }

  def connectToProd(serviceInfo: ServiceInfo): Service = {
    apply(serviceInfo, PROD_SERVER)
  }

  def connectToGlobal(serviceInfo: ServiceInfo): Service = {
    apply(serviceInfo, GLOBAL_PROD_SERVER)
  }
}
