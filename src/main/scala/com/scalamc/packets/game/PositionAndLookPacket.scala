package com.scalamc.packets.game

import com.scalamc.models.VarInt
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class PositionAndLookPacketClient(var x: Double = 0.0,
                                       var y: Double = 0.0,
                                       var z: Double = 0.0,
                                       var yaw: Float = 0.0f,
                                       var pitch: Float = 0.0f,
                                       var flags: Byte = 0,
                                       var teleportId:VarInt = VarInt(0))
  extends Packet(PacketInfo(0x2F.toByte, direction = PacketDirection.Client)){
  def this(){this(0.0)}
}

case class PositionAndLookPacketServer(var x: Double = 0.0,
                                       var y: Double = 0.0,
                                       var z: Double = 0.0,
                                       var yaw: Float = 0.0f,
                                       var pitch: Float = 0.0f,
                                       var onGround: Boolean = true)
  extends Packet(PacketInfo(0x0E.toByte, direction = PacketDirection.Server)){
  def this(){this(0.0)}
}
