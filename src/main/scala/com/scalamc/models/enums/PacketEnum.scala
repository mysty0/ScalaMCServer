package com.scalamc.models.enums

import com.scalamc.utils.ByteStack

object PacketEnum extends Enumeration {
  abstract class EnumVal() extends Val{
    var value: Any

    override def equals(other: Any) = other match {
      case that: EnumVal  => that.value == value
      case _                        => false
    }
  }
}
