package com.scalamc.packets.game

import com.scalamc.models.world.Block
import com.scalamc.models.Position
import com.scalamc.models.utils.VarInt
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class BlockChangePacket(var location: Position = Position(),
                             var id: VarInt = VarInt(0))
  extends Packet(PacketInfo(Map(-1 -> 0x0B.toByte), direction = PacketDirection.Client)) {

  def this(){this(Position())}
}
