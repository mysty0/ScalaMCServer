package com.scalamc.packets.game.player

import com.scalamc.models.Position
import com.scalamc.models.utils.VarInt
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class PlayerDiggingPacket(var status: VarInt = VarInt(0),
                               var position: Position = Position(),
                               var face: Byte = 0)
  extends Packet(PacketInfo(0x14.toByte, direction = PacketDirection.Server)){
  def this(){this(VarInt(0))}

}
