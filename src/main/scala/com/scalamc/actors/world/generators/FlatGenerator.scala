package com.scalamc.actors.world.generators

import akka.actor.{Actor, ActorLogging}
import com.scalamc.models.world.Block
import com.scalamc.models.world.chunk.Chunk

class FlatGenerator extends Actor with ActorLogging{
  override def receive: Receive = {
    case GeneratorsMessages.GenerateChunk(x, z) =>
      log.info("start generate chunk: {} {}", x, z)
      val chunk = new Chunk(x, z)
      for(xx <- 0 until 16)
        for(zz <- 0 until 16)
          chunk.setBlock(xx, 60, zz, Block(2, 0))
      sender() ! GeneratorsMessages.GeneratedChunk(chunk)
      log.info("end generate")
  }
}
