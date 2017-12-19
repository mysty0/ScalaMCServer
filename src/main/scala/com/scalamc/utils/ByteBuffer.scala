package com.scalamc.utils

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class ByteBuffer extends ArrayBuffer[Byte](){

  def this(bytes: Array[Byte]){
    this
    this.insertAll(0, bytes)
  }

  def +=(bytes: Array[Byte]): Unit ={
    this.insertAll(this.length, bytes)
  }

  def +(bytes: Array[Byte]): ByteBuffer ={
    this.insertAll(this.length, bytes)
    this
  }

  def writeVarInt(v: Int): Unit = {
    var value = v
    do {
      var temp = (value & 0x7F).toByte
      // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
      value >>>= 7
      if (value != 0) temp = (temp | 0x80).toByte
      this += temp
    } while (value != 0)
  }

  def readVarInt(ind: Int): Int = BytesUtils.readVarInt(ind, i=>this(i))

}

