package com.scalamc.models.enums

import com.scalamc.utils.ByteStack

object PacketEnum extends Enumeration {
  case class EnumVal(var value: Any) extends Val{

    override def equals(other: Any) = other match {
      case that: EnumVal  => that.value == value
      case _                        => false
    }
  }
}
