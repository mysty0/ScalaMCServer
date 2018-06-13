package com.scalamc.packets.game.entity

import com.scalamc.models.utils.VarInt
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class EntityTeleportPacket(var entityId: VarInt = VarInt(0),
                                var x: Double = 0,
                                var y: Double = 0,
                                var z: Double = 0,
                                var yaw: Byte = 0,
                                var pitch: Byte = 0,
                                var onGround: Boolean = true)
  extends Packet(PacketInfo(Map(-1 -> 0x4B.toByte), direction = PacketDirection.Client)){

  def this(){this(VarInt(0))}
}

