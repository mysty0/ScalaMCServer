package com.scalamc.packets.game

import com.scalamc.models.Position
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class SpawnPositionPacket(var position: Position = Position())
  extends Packet(PacketInfo(0x45.toByte, direction = PacketDirection.Client)){

  def this(){this(Position())}
}
