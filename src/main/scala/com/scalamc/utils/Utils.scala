package com.scalamc.utils

import com.scalamc.models.{Location}

object Utils {

  def locationToRelativeLocation(curLocation: Location, prevLocation: Location): Location = {
    def coordToRelCoord(x: Double, prevX: Double): Double = (x-prevX)*4096D//(x*32d - prevX*32d) * 128d//(x-prevX)*4096D////(x * 32 - prevX * 32) * 128
    Location(coordToRelCoord(curLocation.x, prevLocation.x), coordToRelCoord(curLocation.y, prevLocation.y), coordToRelCoord(curLocation.z, prevLocation.z))
  }

  def angleToByte(angle: Float): Byte = (angle %360  / 360 * 256).toByte//% 360
}
