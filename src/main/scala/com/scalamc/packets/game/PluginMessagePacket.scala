package com.scalamc.packets.game

import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class PluginMessagePacket(var channel: String = "", var data: Array[Byte] = new Array[Byte](0)) extends Packet(PacketInfo(0x09.toByte, direction = PacketDirection.Server)){
  def this(){this("")}
}
