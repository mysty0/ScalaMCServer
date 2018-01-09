package com.scalamc.packets.game.player

import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

class PlayerPositionPacket(var x: Double = 0.0,
                           var y: Double = 0.0,
                           var z: Double = 0.0,
                           var onGround: Boolean = true)
  extends Packet(PacketInfo(0x0D.toByte, direction = PacketDirection.Server)){
  def this(){this(0.0)}
}
