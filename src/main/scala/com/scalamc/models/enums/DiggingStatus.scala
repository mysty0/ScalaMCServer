package com.scalamc.models.enums


object DiggingStatus extends Enumeration {
  type DiggingStatusVal = Value
  //type DiggingStatusVal = Value
  val StartedDigging           = Value
  val CancelledDigging         = Value
  val FinishedDigging          = Value
  val DropItemStack            = Value
  val DropItem                 = Value
  val ShootArrowOrFinishEating = Value
  val SwapItemInHand           = Value

}
