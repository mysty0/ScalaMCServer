package com.scalamc.models.utils

case class VarInt(var int: Int)

object VarInt{
  implicit def int2VarInt(int: Int): VarInt = VarInt(int)
}