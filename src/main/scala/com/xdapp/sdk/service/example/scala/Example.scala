package com.xdapp.sdk.service.example.scala

import com.xdapp.sdk.service.Service
import com.xdapp.sdk.service.model.ServiceInfo

/**
 * @author alusa on 2019/9/27.
 */
object Example {

  def main(args: Array[String]): Unit = {
    val service = Service.connectToLocalDev(ServiceInfo("demo", "gm", "123456"))
    val call = new GmService
    //service.add("hello", call, "gm_hello")
    service.addWebScala("hello", call)
    println(service.getGlobalMethods.getAllNames)
    service.start()
  }
}
