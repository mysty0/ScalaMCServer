package com.scalamc.models

import com.scalamc.models.inventory.InventoryItem

object ProtocolEvents {
  class ProtocolEvent
  case class SetSlotItem(windowId: Int = 0, slotId: Int = 0, slot:InventoryItem) extends ProtocolEvent
}
