package com.scalamc.packets.game

import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}
import com.scalamc.models.VarInt

case class KeepAliveServerPacket(var id: VarInt) extends Packet(PacketInfo(0x1F.toByte, direction = PacketDirection.Client)){
  def this(){this(VarInt(0))}
}
case class KeepAliveClientPacket(var id: VarInt) extends Packet(PacketInfo(0x0B.toByte, direction = PacketDirection.Server)){
  def this(){this(VarInt(0))}
}
