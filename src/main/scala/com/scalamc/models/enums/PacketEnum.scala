package com.scalamc.models.enums

import com.scalamc.utils.ByteStack

object PacketEnum extends Enumeration {
  abstract class EnumVal() extends Val{
    var value: Any
    override def compare(that: PacketEnum.Value): Int = super.compare(that)
  }
}
