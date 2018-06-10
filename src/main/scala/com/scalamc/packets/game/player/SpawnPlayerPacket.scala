package com.scalamc.packets.game.player

import java.util.UUID

import com.scalamc.models.metadata.EntityMetadataRaw
import com.scalamc.models.utils.VarInt
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class SpawnPlayerPacket(var id: VarInt = VarInt(0),
                             var uuid: UUID = UUID.randomUUID(),
                             var x: Double = 0.0,
                             var y: Double = 0.0,
                             var z: Double = 0.0,
                             var yaw: Byte = 0x00.toByte,
                             var pitch: Byte = 0x00.toByte,
                             var metadata: EntityMetadataRaw = new EntityMetadataRaw())
  extends Packet(PacketInfo(Map(-1 -> 0x05.toByte), direction = PacketDirection.Client)) {
  def this(){this(VarInt(0))}
}
