package com.scalamc.models.enums

object PacketEnum extends Enumeration {
  abstract class EnumVal extends Val{
    def toBytes: Array[Byte]
    override def compare(that: PacketEnum.Value): Int = super.compare(that)
  }
}
