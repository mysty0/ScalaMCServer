package com.scalamc.packets.game.player

import com.scalamc.models.utils.VarInt
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class PlayerPositionAndLookPacketClient(var x: Double = 0.0,
                                       var y: Double = 0.0,
                                       var z: Double = 0.0,
                                       var yaw: Float = 0.0f,
                                       var pitch: Float = 0.0f,
                                       var flags: Byte = 0,
                                       var teleportId:VarInt = VarInt(0))
  extends Packet(PacketInfo(Map(340 -> 0x2F.toByte,
                                335 -> 0x2E.toByte),
                direction = PacketDirection.Client)){
  def this(){this(0.0)}
}

case class PlayerPositionAndLookPacketServer(var x: Double = 0.0,
                                       var y: Double = 0.0,
                                       var z: Double = 0.0,
                                       var yaw: Float = 0.0f,
                                       var pitch: Float = 0.0f,
                                       var onGround: Boolean = true)
  extends Packet(PacketInfo(Map(340 -> 0x0E.toByte,
                                335 -> 0x0F.toByte),
                            direction = PacketDirection.Server)){
  def this(){this(0.0)}
}
