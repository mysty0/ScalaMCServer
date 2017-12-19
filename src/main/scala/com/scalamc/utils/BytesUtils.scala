package com.scalamc.utils

import akka.util.ByteString

object BytesUtils {

  implicit def int2Bytes(int: Int): Array[Byte] = Array[Byte]((int >>> 24).asInstanceOf[Byte], (int >>> 16).asInstanceOf[Byte], (int >>> 8).asInstanceOf[Byte], int.asInstanceOf[Byte])//BigInt(int).toByteArray

  implicit def long2Bytes(long: Long): Array[Byte] = {
    var b = new Array[Byte](8)
    for(i <- 0 until b.length){
      b(i) = (long >> (b.length - i - 1 << 3)).toByte
    }
    b
  }

  implicit def string2Bytes(str: String): Array[Byte] = {
    var buff = new ByteBuffer()
    var strBytes = ByteString(str).toArray
    buff.writeVarInt(strBytes.length)
    buff += strBytes
    buff.toArray
  }

  def readVarInt(ind: Int = 0,get: Int=>Byte): Int = {
    var numRead = 0
    var result = 0
    var read = 0.toByte
    var curInt = ind
    do {
      read = get(curInt)
      curInt += 1
      val value = read & 0x7F
      result |= (value << (7 * numRead))
      numRead += 1
      if (numRead > 5) throw new RuntimeException("VarInt is too big")
    } while ((read & 0x80) != 0)
    result
  }

}
