package com.scalamc.models.enums

import com.scalamc.models.enums.PacketEnum.EnumVal

object GameMode extends Enumeration {
  case class GameModeVal(gmId: Byte) extends EnumVal{
    override def toBytes = Array(gmId)
  }
  val Survival = GameModeVal(0)
  val Creative = GameModeVal(1)
  val Adventure = GameModeVal(2)
  val Spectator = GameModeVal(3)
}
