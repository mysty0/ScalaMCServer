package com.scalamc.packets.game.player.inventory

import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class SetSlotPacket(var windowId: Byte = 0,
                         var slotId: Short = 0,
                         var slot: SlotRaw = new SlotRaw())
extends Packet(PacketInfo(0x16.toByte, direction = PacketDirection.Client)){
  def this(){this(0)}
}
