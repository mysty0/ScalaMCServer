package com.scalamc.packets.game.entity

import com.scalamc.models.utils.VarInt
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class EntityLookPacket(var entityId: VarInt = VarInt(0),
                       var yaw: Byte = 0,
                       var pitch: Byte = 0,
                       var onGround: Boolean = true)
  extends Packet(PacketInfo(0x28.toByte, direction = PacketDirection.Client)){

  def this(){this(VarInt(0))}
}
