package com.scalamc.packets.game.player

import com.scalamc.models.Position
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class TabCompletePacket(var text: String = "",
                             var assumeCommand: Boolean = false,
                             var blockPosition: Option[Position] = Some(Position()))
  extends Packet(PacketInfo(Map(340 -> 0x01.toByte, 335 -> 0x02.toByte), direction = PacketDirection.Server)){
  def this(){this("")}
}
