package com.scalamc.packets.game

import com.scalamc.models.utils.VarInt
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class AnimationPacketClient(var entityId: VarInt = VarInt(0), var animationId: Byte = 0)
  extends Packet(PacketInfo(0x06.toByte, direction = PacketDirection.Client)){
  def this(){this(VarInt(0))}
}
case class AnimationPacketServer(var hand: VarInt = VarInt(0))
  extends Packet(PacketInfo(0x1D.toByte, direction = PacketDirection.Server)){
  def this(){this(VarInt(0))}
}