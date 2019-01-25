package com.scalamc.packets.game.player

import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class ChatMessagePacket(var message: String)
  extends Packet(PacketInfo(0x03.toByte, direction = PacketDirection.Server)){
  def this(){this("")}
}
