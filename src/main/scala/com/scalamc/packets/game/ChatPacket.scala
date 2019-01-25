package com.scalamc.packets.game

import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class ChatPacket(var data: String = "",
                      var pos: Byte = 0)
  extends Packet(PacketInfo(0x0F.toByte, direction = PacketDirection.Client)){

  def this(){this("")}

}
