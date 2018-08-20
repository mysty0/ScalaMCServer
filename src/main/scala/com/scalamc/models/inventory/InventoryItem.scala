package com.scalamc.models.inventory

import com.scalamc.models.inventory.ItemType.ItemType
import com.scalamc.packets.game.player.inventory.SlotRaw
import com.xorinc.scalanbt.tags.{TagCompound, TagInt}

case class InventoryItem(var id: Short = 0, var damage: Short = 0, var count: Byte = 0, itemType: ItemType = ItemType.Item, var nBT: TagCompound = new TagCompound(Seq(("", TagInt(0))))) {
  def toRaw: SlotRaw = new SlotRaw(id, Some(count), Some(damage), Some(nBT))
}

object ItemType extends Enumeration{
  type ItemType = Value
  val Block, Item = Value
}
