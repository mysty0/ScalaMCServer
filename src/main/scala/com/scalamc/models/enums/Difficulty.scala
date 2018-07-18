package com.scalamc.models.enums

import com.scalamc.models.enums.PacketEnum.EnumVal

object Difficulty extends Enumeration {
  case class DifficultyVal(override var value: Any = 0) extends EnumVal

  val Peaceful = DifficultyVal(0.toByte)
  val Easy = DifficultyVal(1.toByte)
  val Normal = DifficultyVal(2.toByte)
  val Hard = DifficultyVal(3.toByte)
}
