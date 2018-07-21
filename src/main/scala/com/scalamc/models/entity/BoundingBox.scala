package com.scalamc.models.entity

object BoundingBox{
  def apply(xz: Double, y: Double): BoundingBox = new BoundingBox(xz, y, xz)
}
case class BoundingBox(x: Double, y: Double, z: Double)