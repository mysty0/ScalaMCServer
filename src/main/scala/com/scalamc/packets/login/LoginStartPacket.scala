package com.scalamc.packets.login

import com.scalamc.packets.{Packet, PacketDirection, PacketInfo, PacketState}

//@PacketInfo(0x00.toByte, PackewtState.Login)
case class LoginStartPacket(var name: String = "") extends Packet(PacketInfo(Map(-1 ->0x00.toByte), PacketState.Login, PacketDirection.Server)){
  def this(){this("")}
}
