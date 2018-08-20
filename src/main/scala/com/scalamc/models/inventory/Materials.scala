package com.scalamc.models.inventory

class Materials extends Enumeration {
  class Material(item: InventoryItem) extends Val{
    def this(id: Short, damage: Short) = this(InventoryItem(id, damage))
  }

  val Air = new Material(0, 0)


}
