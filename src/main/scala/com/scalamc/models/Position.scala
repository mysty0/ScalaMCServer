package com.scalamc.models

/**
  * Created by maplegend on 18.12.2017.
  */


case class Position(var x: Int = 0, var y: Int = 0, var z: Int = 0){
  def toLong: Long = ((x & 0x3FFFFFF) << 38) | ((y & 0xFFF) << 26) | (z & 0x3FFFFFF) << 0
}

