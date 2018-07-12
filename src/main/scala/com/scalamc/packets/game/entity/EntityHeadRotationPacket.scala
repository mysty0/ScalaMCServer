package com.scalamc.packets.game.entity

import com.scalamc.models.utils.VarInt
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class EntityHeadRotationPacket (var entityId: VarInt = VarInt(0),
                                var yaw: Byte = 0)
  extends Packet(PacketInfo(Map(-1 -> 0x35.toByte), direction = PacketDirection.Client)){

  def this(){this(VarInt(0))}
}
