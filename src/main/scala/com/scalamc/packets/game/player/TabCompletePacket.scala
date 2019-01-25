package com.scalamc.packets.game.player

import com.scalamc.models.Position
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class TabCompleteRequestPacket(var text: String = "",
                             var assumeCommand: Boolean = false,
                             var blockPosition: Option[Position] = Some(Position()))
  extends Packet(PacketInfo(0x01.toByte, direction = PacketDirection.Server)){
  def this(){this("")}
}
case class TabCompletePacket(var matches: Array[String] = Array())
  extends Packet(PacketInfo(0x0E.toByte, direction = PacketDirection.Client)) {
  def this(){this(Array())}
}
