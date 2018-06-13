package com.scalamc.models.utils

import com.scalamc.packets.NotParsable

@NotParsable case class VarInt(var int: Int)

object VarInt{
  implicit def int2VarInt(int: Int): VarInt = VarInt(int)
}