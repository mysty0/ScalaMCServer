package com.scalamc.packets.game.player.inventory

import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class CreativeInventoryActionPacket(var slot: Short = 0, var item: SlotRaw = new SlotRaw())
  extends Packet(PacketInfo(Map(-1 -> 0x1B.toByte), direction = PacketDirection.Server)){
  def this(){this(0)}
}
