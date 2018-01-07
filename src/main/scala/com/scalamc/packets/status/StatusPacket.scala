package com.scalamc.packets.status

import com.scalamc.packets.{Packet, PacketDirection, PacketInfo, PacketState}

case class StatusPacket(var status: String="") extends Packet(PacketInfo(0x00.toByte, PacketState.Status, PacketDirection.Client)){
  def this(){this("")}
}
