package com.scalamc.models.enums

import com.scalamc.models.enums.PacketEnum.EnumVal

object Difficulty extends Enumeration {
  case class DifficultyVal(dfId: Byte) extends EnumVal{
    override def toBytes: Array[Byte] = Array(dfId)
  }

  val Peaceful = DifficultyVal(0)
  val Easy = DifficultyVal(1)
  val Normal = DifficultyVal(2)
  val Hard = DifficultyVal(3)
}
