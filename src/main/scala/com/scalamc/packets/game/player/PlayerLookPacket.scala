package com.scalamc.packets.game.player

import com.scalamc.models.utils.VarInt
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

class PlayerLookPacket (var yaw: Float = 0.0f,
                        var pitch: Float = 0.0f,
                        var onGround: Boolean = false)
  extends Packet(PacketInfo(0x10.toByte, direction = PacketDirection.Server)){
  def this(){this(0.0f)}
}
