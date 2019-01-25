package com.scalamc.packets.game.entity

import com.scalamc.models.utils.VarInt
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class EntityRelativeMoveAndLookPacket(var entityId: VarInt = VarInt(0),
                                      var deltaX: Short = 0,
                                      var deltaY: Short = 0,
                                      var deltaZ: Short = 0,
                                      var yaw: Byte = 0,
                                      var pitch: Byte = 0,
                                      var onGround: Boolean = true)
  extends Packet(PacketInfo(0x27.toByte, direction = PacketDirection.Client)){

  def this(){this(VarInt(0))}
}
