package com.scalamc.utils

import scala.collection.mutable

class ByteStack extends mutable.ArrayStack[Byte]() {
  def this(bytes: Array[Byte]){
    this
    this ++= bytes.reverse
  }

  def popVarInt(): Int = BytesUtils.readVarInt(get = (i)=>this.pop())

  def popWith(len: Int): ByteBuffer = {
    var packet = new ByteBuffer()
    (0 until len).map((_) => packet += pop())
    packet
  }
}
