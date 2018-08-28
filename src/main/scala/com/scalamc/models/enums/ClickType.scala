package com.scalamc.models.enums

object ClickType extends Enumeration {
  type ClickType = Value
  val PICKUP,
      QUICK_MOVE,
      SWAP,
      CLONE,
      THROW,
      QUICK_CRAFT,
      PICKUP_ALL = Value
}
