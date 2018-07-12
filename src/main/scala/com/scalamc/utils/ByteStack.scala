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

  def popShort(): Short = (((pop()&0xFF)<<8) | (pop()&0xFF)).toShort

  def popUnsignedShort(): Int = ((0xFF & pop()) << 8) | (0xFF&pop())

  def popInt(): Int = (pop() << 24) + (pop() << 16) + (pop() << 8) + (pop() << 0)

  def popLong():Long = {
    var result: Long = 0
    for (i <- 0 until 8) {
      result <<= 8
      result |= (pop() & 0xFF)
    }
    result
  }

  def popDouble(): Double = {
    var i = 0
    var res = 0.toLong
    for (i <- 0 to 7) {
      res += ((pop() & 0xff).toLong << ((7 - i) * 8))
    }
    java.lang.Double.longBitsToDouble(res)
  }
  def popFloat(): Float = java.lang.Float.intBitsToFloat(((pop() & 0xFF) << 24) | ((pop() & 0xFF) << 16) | ((pop() & 0xFF) << 8) | (pop() & 0xFF))//java.lang.Float.intBitsToFloat((pop() & 0xFF) | ((pop() & 0xFF) << 8) | ((pop() & 0xFF) << 16) | ((pop() & 0xFF) << 24))
}
