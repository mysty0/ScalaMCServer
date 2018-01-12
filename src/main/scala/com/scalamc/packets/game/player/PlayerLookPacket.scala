package com.scalamc.packets.game.player

import com.scalamc.models.VarInt
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

class PlayerLookPacket (var yaw: Float = 0.0f,
                        var pitch: Float = 0.0f,
                        var flags: Byte = 0)
  extends Packet(PacketInfo(Map(340 -> 0x0F.toByte,
                                335 -> 0x10.toByte),
                            direction = PacketDirection.Server)){
  def this(){this(0.0f)}
}
