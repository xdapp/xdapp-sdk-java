package com.xdapp.sdk.service

import com.xdapp.sdk.service.protocol.v1.Message
import hprose.server.{HproseClients, ServiceContext}

/**
 * @author alusa on 2019/9/27.
 */
class BasicContext(message: Message, clients: HproseClients) extends ServiceContext(clients) {
  def header() = {
    message.header
  }

  def prefix() = {
    message.prefix
  }
}
