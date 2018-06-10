package com.scalamc.packets.status

import com.scalamc.packets.{Packet, PacketDirection, PacketInfo, PacketState}

case class PongPacket(var payload: Long = 0)
  extends Packet(PacketInfo(Map(-1 -> 0x01.toByte), PacketState.Status, PacketDirection.Client)){
  def this(){this(0)}
}
