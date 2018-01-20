package com.scalamc.models

case class Location(var x: Double = 0,
                    var y: Double = 0,
                    var z: Double = 0,
                    var yaw: Float = 0.0f,
                    var pitch: Float = 0.0f) {
  def toPosition = Position(x.toInt, y.toInt, z.toInt)
}
