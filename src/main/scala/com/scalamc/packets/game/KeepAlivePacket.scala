package com.scalamc.packets.game

import com.scalamc.models.utils.VarInt
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class KeepAliveServerPacket(var id: Long)
  extends Packet(PacketInfo(Map(340 -> 0x1F.toByte), direction = PacketDirection.Client)){

  def this(){this(0)}
}
case class KeepAliveClientPacket(var id: Long)
  extends Packet(PacketInfo(Map(340 -> 0x0B.toByte), direction = PacketDirection.Server)){

  def this(){this(0)}
}

case class KeepAliveServerPacketOld(var id: VarInt)
  extends Packet(PacketInfo(Map(335 -> 0x1F.toByte), direction = PacketDirection.Client)){

  def this(){this(VarInt(0))}
}
case class KeepAliveClientPacketOld(var id: VarInt)
  extends Packet(PacketInfo(Map(335 -> 0x0C.toByte), direction = PacketDirection.Server)){

  def this(){this(VarInt(0))}
}
