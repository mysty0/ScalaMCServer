package com.scalamc.packets.game

import com.scalamc.models.utils.VarInt
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class KeepAliveServerPacket(var id: Long)
  extends Packet(PacketInfo(0x1F.toByte, direction = PacketDirection.Client)){

  def this(){this(0)}
}
case class KeepAliveClientPacket(var id: Long)
  extends Packet(PacketInfo(0x0C.toByte, direction = PacketDirection.Server)){

  def this(){this(0)}
}
