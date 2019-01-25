package com.scalamc.packets.game

import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class DisconnectPacket(var reason: String = "")
  extends Packet(PacketInfo(0x1A.toByte, direction = PacketDirection.Client)){
  def this(){this("")}
}
