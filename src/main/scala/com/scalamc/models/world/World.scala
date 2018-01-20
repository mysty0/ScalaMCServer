package com.scalamc.models.world

import com.scalamc.models.Location
import com.scalamc.models.world.chunk.generators.FlatChunkGenerator
import com.scalamc.models.world.chunk.{Chunk, ChunkGenerator}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class World(chunkGenerator: ChunkGenerator = new FlatChunkGenerator()){
  var chunks = new mutable.HashMap[Long,Chunk]()

  def getChunk(x: Int, z: Int): Chunk = {
    val key = (x.toLong << 32) | (z & 0xffffffffL)
    if(!chunks.contains(key))
      chunks(key) = chunkGenerator.generateChunk(x, z, 123)
    chunks(key)

  }
  def setChunk(x: Int, z: Int, chunk: Chunk) = chunks((x.toLong << 32) | (z & 0xffffffffL)) = chunk

  def getChunksForDistance(location: Location, distance: Int): ArrayBuffer[Chunk] = {
    var chunks = ArrayBuffer[Chunk]()

    val pos = location.toPosition

    val centralX = pos.x >> 4
    val centralZ = pos.z >> 4

    println("ceb x", centralX)

    for(x <- centralX - distance to centralZ+distance)
      for(z <- centralZ - distance to centralZ + distance){
        chunks += getChunk(x, z)
      }
    //chunks += getChunk(0,0)
    println(chunks.length)
    chunks
  }
  
}