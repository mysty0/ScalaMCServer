package com.scalamc.models.enums

import com.scalamc.models.enums.PacketEnum.EnumVal
import com.scalamc.utils.BytesUtils._

object Dimension extends Enumeration {
  type Dimension = EnumVal

  val Nether = EnumVal(value = -1)
  val Overworld = EnumVal(value = 0)
  val End = EnumVal(value = 1)


}
