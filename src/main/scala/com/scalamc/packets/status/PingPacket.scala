package com.scalamc.packets.status

import com.scalamc.packets.{Packet, PacketDirection, PacketInfo, PacketState}

case class PingPacket(var payload: Long = 0)
  extends Packet(PacketInfo(0x01.toByte, PacketState.Status, PacketDirection.Server)){
  def this(){this(0)}
}
