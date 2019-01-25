package com.scalamc.packets.game.player

import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class HeldItemChangePacketServer(var slot: Short)
  extends Packet(PacketInfo(0x1A.toByte, direction = PacketDirection.Server)){
  def this(){this(0)}
}

case class HeldItemChangePacketClient(var slot: Byte)
  extends Packet(PacketInfo(0x3A.toByte, direction = PacketDirection.Client)){
  def this(){this(0)}
}
