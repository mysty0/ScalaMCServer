package com.scalamc.packets.game

import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class UnloadChunkPacket(var chunkX: Int = 0, var chunkZ: Int = 0)
  extends Packet(PacketInfo(0x1D.toByte, direction = PacketDirection.Client)) {
  def this(){this(0)}
}
