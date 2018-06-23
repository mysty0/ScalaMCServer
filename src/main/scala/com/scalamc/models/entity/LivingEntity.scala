package com.scalamc.models.entity

import com.scalamc.models.inventory.Inventory

trait LivingEntity extends Entity {

  var inventory: Inventory = new Inventory()
  var selectedSlot: Byte = 0
}
