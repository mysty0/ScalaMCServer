package com.scalamc.actors

import akka.actor.{Actor, ActorRef, Props}
import com.scalamc.actors.World.GetChunksForDistance
import com.scalamc.models.Events._
import com.scalamc.models.{Location, Player}
import com.scalamc.models.world.chunk.generators.FlatChunkGenerator
import com.scalamc.models.world.chunk.{Chunk, ChunkGenerator}
import com.scalamc.utils.Utils

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

  def needTeleport(dx: Double, dy: Double, dz: Double): Boolean = dx > Short.MaxValue || dy > Short.MaxValue || dz > Short.MaxValue || dx < Short.MinValue || dy < Short.MinValue || dz < Short.MinValue

  override def receive = {
    case GetChunksForDistance(location, distance) =>
      val pos = location.toPosition

      val centralX = pos.x >> 4
      val centralZ = pos.z >> 4

      println("ceb x", centralX)

      for(x <- centralX - distance to centralZ+distance)
        for(z <- centralZ - distance to centralZ + distance){
          sender() ! getChunk(x, z)
        }

    case JoinPlayerEvent(p) =>
      players += p
      players.filter(_.uuid != p.uuid).foreach(pl => {
        pl.session.self ! JoinPlayerEvent(p)
        p.session.self ! JoinPlayerEvent(pl)
      })
    case Disconnect(p, r) =>
      players -= p
      players.filter(_.uuid != p.uuid).foreach(pl => pl.session.self ! Disconnect(p, r))

    case GetPlayersPosition(pl) =>

      players.filter(_.uuid != pl.uuid).foreach(p => pl.session.self ! TeleportEntity(p.entityId, p.position))

    case ChangePlayerPosition(pl, loc, prLoc) =>
      var relLoc = Utils.locationToRelativeLocation(loc, prLoc)
      pl.position = loc
      val needTp = needTeleport(relLoc.x, relLoc.y, relLoc.z)
      players.filter(_.uuid != pl.uuid).foreach(p => {if(!needTp) p.session.self ! RelativePlayerMove(pl, relLoc.x.toShort, relLoc.y.toShort, relLoc.z.toShort)
      else p.session.self ! TeleportEntity(pl.entityId,loc)})
    case ChangePlayerPositionAndLook(pl, loc, prLoc) =>
      var relLoc = Utils.locationToRelativeLocation(loc, prLoc)
      val angYaw = Utils.angleToByte(loc.yaw)
      val angPitch = Utils.angleToByte(loc.pitch)
      pl.position = loc
      val needTp = needTeleport(relLoc.x, relLoc.y, relLoc.z)
      players.filter(_.uuid != pl.uuid).foreach(p => {if(!needTp) p.session.self ! RelativePlayerMoveAndLook(pl, relLoc.x.toShort, relLoc.y.toShort, relLoc.z.toShort, angYaw, angPitch)
                                                      else p.session.self ! TeleportEntity(pl.entityId,loc)})
    case ChagePlayerLook(pl, yaw, pitch) =>
      val angYaw = Utils.angleToByte(yaw)
      val angPitch = Utils.angleToByte(pitch)
      pl.position.yaw = yaw
      pl.position.pitch = pitch
      players.filter(_.uuid != pl.uuid).foreach(_.session.self ! ChangeEntityLook(pl.entityId, angYaw, angPitch))
  }


}