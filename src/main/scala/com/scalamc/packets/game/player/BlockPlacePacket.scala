package com.scalamc.packets.game.player

import com.scalamc.models.Position
import com.scalamc.models.utils.VarInt
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class BlockPlacePacket(var position: Position = Position(),
                            var face: VarInt = VarInt(0),
                            var hand: VarInt = VarInt(0),
                            var cursorPositionX: Float = 0,
                            var cursorPositionY: Float = 0,
                            var cursorPositionZ: Float = 0)
  extends Packet(PacketInfo(Map(-1 -> 0x1F.toByte), direction = PacketDirection.Server)){
  def this(){this(Position())}
}
