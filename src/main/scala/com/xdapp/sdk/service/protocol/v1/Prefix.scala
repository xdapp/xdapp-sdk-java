package com.xdapp.sdk.service.protocol.v1

import scodec.Codec
import scodec.codecs._

/**
 * @author alusa on 2019/9/27.
 */
case class Prefix(flag: Int, ver: Int, length: Long) {
  def checkFlag(target: Int): Boolean = (flag & target) == target

  def isResult: Boolean = (flag & Constant.FLAG_RESULT_MODE) == Constant.FLAG_RESULT_MODE
}

object Prefix {
  implicit val codec: Codec[Prefix] = (uint8 :: uint8 :: uint32).as[Prefix]
}