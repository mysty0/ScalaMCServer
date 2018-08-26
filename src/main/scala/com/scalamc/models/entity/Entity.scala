package com.scalamc.models.entity

import com.scalamc.actors.world.World
import com.scalamc.models.Location

trait Entity {
  var hasRotate: Boolean = false
  var hasMove: Boolean = false

  val entityId: Int
  var location: Location
  var previousLocation:Location

  val typeId: Int
  val boundingBox: BoundingBox
  val nameId: String
}
