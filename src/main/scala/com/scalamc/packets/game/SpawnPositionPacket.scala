package com.scalamc.packets.game

import com.scalamc.models.Position
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class SpawnPositionPacket(var position: Position = Position())
  extends Packet(PacketInfo(Map(340 -> 0x46.toByte), direction = PacketDirection.Client)){

  def this(){this(Position())}
}
