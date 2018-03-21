package com.scalamc.actors

import akka.actor.{Actor, ActorRef, Props}
import com.scalamc.actors.World.GetChunksForDistance
import com.scalamc.models.Events.{ChangePosition, JoinPlayerEvent}
import com.scalamc.models.{Location, Player}
import com.scalamc.models.world.chunk.generators.FlatChunkGenerator
import com.scalamc.models.world.chunk.{Chunk, ChunkGenerator}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer


object World{

  case class GetChunksForDistance(location: Location, distance: Int)

  def props(chunkGenerator: ChunkGenerator = new FlatChunkGenerator()) = Props(
    new World(chunkGenerator)
  )
}

class World(chunkGenerator: ChunkGenerator) extends Actor{
  var chunks = new mutable.HashMap[Long,Chunk]()

  var players = ArrayBuffer[Player]()

  def getChunk(x: Int, z: Int): Chunk = {
    val key = (x.toLong << 32) | (z & 0xffffffffL)
    if(!chunks.contains(key))
      chunks(key) = chunkGenerator.generateChunk(x, z, 123)
    chunks(key)

  }
  def setChunk(x: Int, z: Int, chunk: Chunk) = chunks((x.toLong << 32) | (z & 0xffffffffL)) = chunk

  override def receive = {
    case GetChunksForDistance(location, distance) =>
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
      sender() ! chunks
    case JoinPlayerEvent(p) =>
      players += p

    case ChangePosition(loc) =>
      //players.foreach(_.session ! )
  }


}