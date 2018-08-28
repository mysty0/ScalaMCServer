package com.scalamc.models.enums

import com.scalamc.models.enums.PacketEnum.EnumVal

object GameMode extends Enumeration {
  type GameMode = EnumVal
  val Survival = EnumVal(value = 0.toByte)
  val Creative = EnumVal(value = 1.toByte)
  val Adventure = EnumVal(value = 2.toByte)
  val Spectator = EnumVal(value = 3.toByte)
}
