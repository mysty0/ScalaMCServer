package com.scalamc.models.inventory

import com.scalamc.packets.game.player.inventory.SlotRaw
import com.xorinc.scalanbt.tags.{TagCompound, TagInt}

class InventoryItem(var id: Short = 0, var damage: Short = 0, var count: Byte = 0, var nBT: TagCompound = new TagCompound(Seq(("", TagInt(0))))) {
  def toRaw: SlotRaw = new SlotRaw(id, Some(count), Some(damage), Some(nBT))
}
