package com.scalamc.packets.game.entity.spawn

import java.util.UUID

import com.scalamc.models.Position
import com.scalamc.models.utils.VarInt
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class SpawnPaintingPacket(var entityId: VarInt = 0,
                               var uuid: UUID = UUID.randomUUID(),
                               var title: String = "",
                               var position: Position = Position(),
                               var direction: Byte = 0)
  extends Packet(PacketInfo(0x04.toByte, direction = PacketDirection.Client)) {

  def this(){this(0)}
}
