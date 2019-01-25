package com.scalamc.packets.game.entity

import com.scalamc.models.utils.VarInt
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class EntityRelativeMovePacket(var entityId: VarInt = VarInt(0),
                               var deltaX: Short = 0,
                               var deltaY: Short = 0,
                               var deltaZ: Short = 0,
                               var onGround: Boolean = true)
  extends Packet(PacketInfo(0x26.toByte, direction = PacketDirection.Client)){

  def this(){this(VarInt(0))}
}
