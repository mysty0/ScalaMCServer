package com.scalamc.packets.login

import com.scalamc.packets.{Packet, PacketDirection, PacketInfo, PacketState}

case class LoginSuccessPacket(var uuid: String = "", var name: String = "") extends Packet(PacketInfo(0x02.toByte, PacketState.Login, PacketDirection.Client)) {
  def this(){this("")}
}
