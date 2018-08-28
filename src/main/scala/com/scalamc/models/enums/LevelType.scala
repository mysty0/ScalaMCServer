package com.scalamc.models.enums

import com.scalamc.models.enums.PacketEnum.EnumVal
import com.scalamc.utils.BytesUtils._

object LevelType extends Enumeration {
  type LevelType = EnumVal

  val Default = EnumVal(value = "default")
  val Flat = EnumVal(value = "flat")
  val LargeBiomes = EnumVal(value = "largeBiomes")
  val Amplified = EnumVal(value = "amplified")
  val Default_1_1 = EnumVal(value = "default_1_1")

}
