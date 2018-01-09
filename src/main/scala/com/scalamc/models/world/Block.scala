package com.scalamc.models.world

import com.scalamc.models.VarInt

object Block{
  implicit def blockToVarInt(block: Block) = VarInt(block.id << 4 | (block.metadata & 15))
}

case class Block(var id: Int, var metadata: Byte) {

}
