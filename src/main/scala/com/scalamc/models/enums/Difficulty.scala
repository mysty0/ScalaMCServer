package com.scalamc.models.enums

import com.scalamc.models.enums.PacketEnum.EnumVal

object Difficulty extends Enumeration {
  type Difficulty = EnumVal

  val Peaceful = EnumVal(value = 0.toByte)
  val Easy = EnumVal(value = 1.toByte)
  val Normal = EnumVal(value = 2.toByte)
  val Hard = EnumVal(value = 3.toByte)
}
