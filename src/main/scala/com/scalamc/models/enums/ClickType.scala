package com.scalamc.models.enums

object ClickType extends Enumeration {
  type ClickType = Value
  val Click,
      ShiftClick,
      NumberKeyPress,
      MiddleClick,
      Throw,
      MouseDrag,
      DoubleClick = Value
}
