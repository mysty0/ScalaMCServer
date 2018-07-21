package com.scalamc.packets.game.player

import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class ChatMessagePacket(var message: String)
  extends Packet(PacketInfo(Map(340 -> 0x02.toByte, 335 -> 0x03.toByte), direction = PacketDirection.Server)){
  def this(){this("")}
}
