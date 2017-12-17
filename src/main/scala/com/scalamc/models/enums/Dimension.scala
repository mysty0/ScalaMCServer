package com.scalamc.models.enums

import com.scalamc.models.enums.PacketEnum.EnumVal
import com.scalamc.utils.BytesUtils._

object Dimension extends Enumeration {
  case class DimensionVal(dmId: Int) extends EnumVal{
    override def toBytes: Array[Byte] = dmId
  }

  val Nether = DimensionVal(-1)
  val Overworld = DimensionVal(0)
  val End = DimensionVal(1)


}
