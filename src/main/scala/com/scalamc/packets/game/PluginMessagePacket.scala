package com.scalamc.packets.game

import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class PluginMessagePacketClient(var channel: String = "",
                               var data: Array[Byte] = new Array[Byte](0))
  extends Packet(PacketInfo(0x0A.toByte, direction = PacketDirection.Server)){

  def this(){this("")}
}
case class PluginMessagePacketServer(var channel: String = "",
                        var data: Array[Byte] = Array[Byte]())
  extends Packet(PacketInfo(0x18.toByte, direction = PacketDirection.Client)){
  def this(){this("")}
}

