package com.scalamc.packets.login

import com.scalamc.packets.{Packet, PacketDirection, PacketInfo, PacketState}

class LoginSuccessPacket(var uuid: String = "", var name: String = "") extends Packet(PacketInfo(0x00.toByte, PacketState.Login, PacketDirection.Client)) {
  def this(){this("","")}
}
