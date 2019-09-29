package com.xdapp.sdk.service.protocol.v1

import scodec.Codec
import scodec.codecs._

/**
 * @author alusa on 2019/9/27.
 */
case class Header(appId: Long,
                  serviceId: Long,
                  requestId: Long,
                  adminId: Long,
                  contextLength: Int)

object Header {
  implicit val codec: Codec[Header] =
    (uint32 :: uint32 :: uint32 :: uint32 :: uint8).as[Header]
}