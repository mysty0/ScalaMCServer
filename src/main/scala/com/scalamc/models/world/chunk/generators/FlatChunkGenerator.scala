package com.scalamc.models.world.chunk.generators

import com.scalamc.models.world.Block
import com.scalamc.models.world.chunk.{Chunk, ChunkGenerator}

class FlatChunkGenerator extends ChunkGenerator{
  override def generateChunk(x: Int, z: Int, seed: Int) = {
    val chunk = new Chunk(x, z)
    for(xx <- 0 until 16)
      for(zz <- 0 until 16)
        chunk.setBlock(xx, 2, zz, Block(1, 0))
    chunk
  }
}
