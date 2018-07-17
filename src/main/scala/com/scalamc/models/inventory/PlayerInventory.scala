package com.scalamc.models.inventory

class PlayerInventory extends Inventory {
  override var items: Array[InventoryItem] = new Array[InventoryItem](45)

  def headIten: InventoryItem = items(5)
  def chestIten: InventoryItem = items(6)
  def legIten: InventoryItem = items(7)
  def feedIten: InventoryItem = items(8)
}
