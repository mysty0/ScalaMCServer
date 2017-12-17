package com.scalamc.utils

import akka.util.ByteString

object BytesUtils {

  implicit def int2Bytes(int: Int): Array[Byte] = BigInt(int).toByteArray

  implicit def string2Bytes(str: String): Array[Byte] = {
    var buff = new ByteBuffer()
    var strBytes = ByteString(str).toArray
    buff.writeVarInt(strBytes.length)
    buff += strBytes
    buff.toArray
  }

}
