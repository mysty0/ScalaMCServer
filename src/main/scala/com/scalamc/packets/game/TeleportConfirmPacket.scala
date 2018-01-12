package com.scalamc.packets.game

import com.scalamc.models.VarInt
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class TeleportConfirmPacket(var id: VarInt = VarInt(0))
  extends Packet(PacketInfo(Map(340 -> 0x00.toByte), direction = PacketDirection.Server)){

  def this(){this(VarInt(0))}
}
