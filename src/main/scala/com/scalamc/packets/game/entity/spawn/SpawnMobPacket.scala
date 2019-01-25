package com.scalamc.packets.game.entity.spawn

import java.util.UUID

import com.scalamc.models.RawLocation
import com.scalamc.models.metadata.EntityMetadataRaw
import com.scalamc.models.utils.VarInt
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class SpawnMobPacket(var entityId: VarInt = 0,
                          var uuid: UUID = UUID.randomUUID(),
                          var mobType: VarInt = 0,
                          var location: RawLocation = RawLocation(),
                          var headPitch: Byte = 0,
                          var velocityX: Short = 0,
                          var velocityY: Short = 0,
                          var velocityZ: Short = 0,
                          var metadata: EntityMetadataRaw = new EntityMetadataRaw())
  extends Packet(PacketInfo(0x03.toByte, direction = PacketDirection.Client)) {
  def this(){this(VarInt(0))}

}
