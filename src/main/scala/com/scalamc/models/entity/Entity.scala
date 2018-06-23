package com.scalamc.models.entity

import com.scalamc.actors.World
import com.scalamc.models.Location

trait Entity {
  var hasRotate: Boolean = false
  var hasMove: Boolean = false

  val entityId: Int
  var location: Location
  var previousLocation:Location
}
