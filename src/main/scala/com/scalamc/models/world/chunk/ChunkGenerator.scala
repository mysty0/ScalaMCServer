package com.scalamc.models.world.chunk

abstract class ChunkGenerator {

  def generateChunk(x: Int, z: Int, seed: Int): Chunk

}
