package com.scalamc.models.enums

import com.scalamc.models.Position

object BlockFace extends Enumeration {
  case class BlockFaceVal(posMod: Position) extends Val

  val Bottom  = BlockFaceVal(Position( 0,-1, 0))//-Y
  val Top     = BlockFaceVal(Position( 0, 1, 0))//+Y
  val North   = BlockFaceVal(Position( 0, 0,-1))//-Z
  val South   = BlockFaceVal(Position( 0, 0, 1))//+Z
  val West    = BlockFaceVal(Position(-1, 0, 0))//-X
  val East    = BlockFaceVal(Position( 1, 0, 0))//+X
}
