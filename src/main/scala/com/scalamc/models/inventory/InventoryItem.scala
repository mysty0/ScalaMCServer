package com.scalamc.models.inventory

import com.scalamc.packets.game.player.inventory.SlotRaw
import com.xorinc.scalanbt.tags.{TagCompound, TagInt}

object InventoryItem{
  def apply(slotRaw: SlotRaw): InventoryItem = {
    Items.getClass.getDeclaredFields map {f => f.setAccessible(true); f.get(Items)} filter {_ != Items} map {_.asInstanceOf[InventoryItem]} find { item => item.id == slotRaw.itemId && item.metadata == slotRaw.itemDamage.getOrElse(0) } getOrElse new InventoryItem(0)
  }//def apply(id: Int = 0, metadata: Int = 0, count: Byte = 0, nBT: TagCompound = new TagCompound(Seq(("", TagInt(0))))): InventoryItem = null
}

class InventoryItem(val id: Int = 0, val metadata: Int = 0, var count: Int = 0, val stackLimit: Int = 64, var nBT: TagCompound = new TagCompound(Seq(("", TagInt(0))))) {
  def toRaw: SlotRaw = new SlotRaw(id.toShort, Some(count.toByte), Some(metadata.toShort), Some(nBT))

  override def equals(obj: scala.Any): Boolean = obj match {
    case item: InventoryItem =>
      item.id == id && item.metadata == metadata
    case _ =>
      false
  }

  override def toString: String = s"Item id: $id count: $count"
}
