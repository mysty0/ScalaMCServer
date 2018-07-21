package com.scalamc.models

import com.scalamc.utils.Utils

object RawLocation{
  def apply(location: Location): RawLocation = RawLocation(location.x, location.y, location.z, Utils.angleToByte(location.yaw), Utils.angleToByte(location.pitch))
}

case class RawLocation(var x: Double = 0,
                  var y: Double = 0,
                  var z: Double = 0,
                  var yaw: Byte = 0,
                  var pitch: Byte = 0) {
  def this(){this(0)}
}
