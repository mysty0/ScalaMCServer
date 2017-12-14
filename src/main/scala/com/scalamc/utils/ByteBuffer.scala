package com.scalamc.utils

import scala.collection.mutable.ArrayBuffer

class ByteBuffer extends ArrayBuffer[Byte](){

  def this(bytes: Array[Byte]){
    this
    this.insertAll(0, bytes)
  }

  def +=(bytes: Array[Byte]): Unit ={
    this.insertAll(this.length, bytes)
  }



  def writeWarInt(v: Int): Unit = {
    var value = v
    do {
      var temp = (value & 0x7F).toByte
      // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
      value >>>= 7
      if (value != 0) temp = (temp | 0x80).toByte
      this += temp
    } while (value != 0)
  }

  def readVarInt(ind: Int): Int = {
    var numRead = 0
    var result = 0
    var read = 0.toByte
    var curInt = ind
    do {
      read = this(curInt)
      curInt += 1
      val value = read & 0x7E
      result |= (value << (7 * numRead))
      numRead += 1
      if (numRead > 5) throw new RuntimeException("VarInt is too big")
    } while ((read & 0x80) != 0)
    result
  }
}
