package com.scalamc.models.utils

import com.scalamc.packets.NotParsable
import com.scalamc.utils.ByteBuffer

import scala.collection.mutable.ArrayBuffer

@NotParsable case class VarInt(var int: Int){
  def toBytes: ArrayBuffer[Byte] = {
    val bf = new ByteBuffer()
    bf.writeVarInt(int)
    bf
  }
}

object VarInt{
  implicit def int2VarInt(int: Int): VarInt = VarInt(int)
}