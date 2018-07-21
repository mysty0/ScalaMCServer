package com.scalamc.models.entity.mob

import com.scalamc.models.Location
import com.scalamc.models.entity.{BoundingBox, Mob}
import com.scalamc.models.inventory.Inventory

case class Zombie() extends Mob{
  override var inventory: Inventory = _
  override val entityId: Int = 0
  override var location: Location = _
  override var previousLocation: Location = _
  override val typeId: Int = 54
  override val boundingBox: BoundingBox = BoundingBox(0.6, 1.95)
  override val nameId: String = "zombie"
}
