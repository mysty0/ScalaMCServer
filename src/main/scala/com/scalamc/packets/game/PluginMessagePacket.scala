package com.scalamc.packets.game

import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class PluginMessagePacketClient(var channel: String = "",
                               var data: Array[Byte] = new Array[Byte](0))
  extends Packet(PacketInfo(Map(340 -> 0x09.toByte), direction = PacketDirection.Server)){

  def this(){this("")}
}
case class PluginMessagePacketServer(var channel: String = "",
                        var data: Array[Byte] = Array[Byte]())
  extends Packet(PacketInfo(Map(-1 -> 0x18.toByte), direction = PacketDirection.Client)){
  def this(){this("")}
}

