package com.scalamc.utils

import java.nio.ByteBuffer
import scala.collection.mutable


object VarInt {

  def writeVarInt[T <: mutable.Buffer[Byte]](v: Int, dest: T): Unit = {
    var value = v
    do {
      var temp = (value & 0x7F).toByte
      // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
      value >>>= 7
      if (value != 0) temp = (temp | 0x80).toByte
        dest += temp
    } while (value != 0)
  }

  def readVarInt(): Int = {
    var numRead = 0
    var result = 0
    var read = 0
    do {
      read = 0
      val value = read & 0x01111111
        result |= (value << (7 * numRead))
      numRead += 1
      if (numRead > 5) throw new RuntimeException("VarInt is too big")
    } while ((read & 0x10000000) != 0)
    result
  }

}
