package com.scalamc.packets.game.player.inventory

import com.scalamc.models.utils.VarInt
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class ClickWindowPacket(var windowId: Byte = 0,
                             var slot: Short = 0,
                             var button: Byte = 0,
                             var actionNumber: Short = 0,
                             var mode: VarInt = VarInt(0),
                             var clickedItem: SlotRaw = new SlotRaw())
  extends Packet(PacketInfo(0x07.toByte, direction = PacketDirection.Server)){
  def this(){this(0)}
}
