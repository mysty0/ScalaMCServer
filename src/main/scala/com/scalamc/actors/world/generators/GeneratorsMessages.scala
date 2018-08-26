package com.scalamc.actors.world.generators

import com.scalamc.models.world.chunk.Chunk

object GeneratorsMessages {
  case class GenerateChunk(x: Int, z: Int)
  case class GeneratedChunk(chunk: Chunk)
}
