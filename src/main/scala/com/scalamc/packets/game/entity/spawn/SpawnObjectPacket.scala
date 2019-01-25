package com.scalamc.packets.game.entity.spawn

import java.util.UUID

import com.scalamc.models.utils.VarInt
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class SpawnObjectPacket(var entityId: VarInt = 0,
                             var uuid : UUID = UUID.randomUUID(),
                             var objectType: Byte = 0,
                             var x: Double = 0,
                             var y: Double = 0,
                             var z: Double = 0,
                             var yaw: Byte = 0x00.toByte,
                             var pitch: Byte = 0x00.toByte,
                             var velocityX: Short = 0,
                             var velocityY: Short = 0,
                             var velocityZ: Short = 0)
  extends Packet(PacketInfo(0x00.toByte, direction = PacketDirection.Client)) {

  def this(){this(0)}
}
