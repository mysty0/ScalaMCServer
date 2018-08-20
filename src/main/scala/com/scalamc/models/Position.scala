package com.scalamc.models

/**
  * Created by maplegend on 18.12.2017.
  */


case class Position(var x: Int = 0, var y: Int = 0, var z: Int = 0){
  def toLong: Long = ((x.toLong & 0x3FFFFFF) << 38) | ((y.toLong & 0xFFF) << 26) | (z.toLong & 0x3FFFFFF)

  def toChunkRelativePosition: Position = {
    def normalizeToChunk(x: Int): Int = if(x < 0) 16+x%16 else x%16
    Position(normalizeToChunk(x), y, normalizeToChunk(z))
  }

  def +(position: Position) = Position(x+position.x, y+position.y, z+position.z)
}

