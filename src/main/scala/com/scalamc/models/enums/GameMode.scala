package com.scalamc.models.enums

import com.scalamc.models.enums.PacketEnum.EnumVal

object GameMode extends Enumeration {
  case class GameModeVal(override var value: Any) extends EnumVal
  val Survival = GameModeVal(0.toByte)
  val Creative = GameModeVal(1.toByte)
  val Adventure = GameModeVal(2.toByte)
  val Spectator = GameModeVal(3.toByte)
}
