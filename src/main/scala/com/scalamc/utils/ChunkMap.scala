package com.scalamc.utils


import com.scalamc.models.world.chunk.Chunk

class ChunkMap {
  var map = collection.mutable.Map[Long, Chunk]()
  def apply(x: Int, z: Int) = map((x << 32) | (z & 0xffffffffL))
  def update(x: Int, z: Int, value: Chunk) = map((x << 32) | (z & 0xffffffffL)) = value
}
