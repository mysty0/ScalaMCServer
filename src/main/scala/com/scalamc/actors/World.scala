package com.scalamc.actors

import akka.actor.{Actor, ActorRef, Cancellable, Props}
import com.scalamc.actors.Session.AddNewPlayer
import com.scalamc.actors.World._
import com.scalamc.models.entity.Entity
import com.scalamc.models.{Chat, Location, Player}
import com.scalamc.models.world.chunk.generators.FlatChunkGenerator
import com.scalamc.models.world.chunk.{Chunk, ChunkGenerator}
import com.scalamc.utils.Utils

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._


object World{

  case class GetChunksForDistance(location: Location, distance: Int)

  def props(chunkGenerator: ChunkGenerator = new FlatChunkGenerator()) = Props(
    new World(chunkGenerator)
  )

  private val period = 100.millisecond

  case object  Tick
  case class JoinPlayer(player: Player)
  case class DisconnectPlayer(player: Player, reason: Chat)
  case class UpdateEntityPosition(entityId: Int, loc: Location)
  case class UpdateEntityPositionAndLook(entityId: Int, loc: Location)
  case class UpdateEntityLook(entityId: Int, loc: Location)
  case class AnimateEntity(entityId: Int, animationId: Byte)
}

class World(chunkGenerator: ChunkGenerator) extends Actor{
  import context._

  var chunks = new mutable.HashMap[Long,Chunk]()
  var entities: ArrayBuffer[Entity] = ArrayBuffer[Entity]()
  var players: ArrayBuffer[Player] = ArrayBuffer[Player]()
  private var scheduler: Cancellable = _

  override def preStart(): Unit = {
    scheduler = context.system.scheduler.schedule(period, period, self, Tick)
  }

  def getChunk(x: Int, z: Int): Chunk = {
    val key = (x.toLong << 32) | (z & 0xffffffffL)
    if(!chunks.contains(key))
      chunks(key) = chunkGenerator.generateChunk(x, z, 123)
    chunks(key)

  }
  def setChunk(x: Int, z: Int, chunk: Chunk) = chunks((x.toLong << 32) | (z & 0xffffffffL)) = chunk

  def needTeleport(dx: Double, dy: Double, dz: Double): Boolean = dx > Short.MaxValue || dy > Short.MaxValue || dz > Short.MaxValue || dx < Short.MinValue || dy < Short.MinValue || dz < Short.MinValue

  override def receive = {
    case Tick =>
      entities.filter(e => e.hasRotate || e.hasMove).foreach{ent =>
        if(ent.hasMove) {
          val relLoc = Utils.locationToRelativeLocation(ent.location, ent.previousLocation)
          if (ent.hasRotate) {
            val angYaw = Utils.angleToByte(ent.location.yaw)
            val angPitch = Utils.angleToByte(ent.location.pitch)
            players.filter(_.entityId != ent.entityId ).foreach {pl =>
                pl.session.self ! Session.RelativeMoveAndLook(ent.entityId, relLoc.x.toShort, relLoc.y.toShort, relLoc.z.toShort, angYaw, angPitch)
            }
          }else{
            players.filter(_.entityId != ent.entityId ).foreach {pl =>
                pl.session.self ! Session.RelativeMove(ent.entityId, relLoc.x.toShort, relLoc.y.toShort, relLoc.z.toShort)
            }
          }
        }
        else if(ent.hasRotate){
          val angYaw = Utils.angleToByte(ent.location.yaw)
          val angPitch = Utils.angleToByte(ent.location.pitch)
          players.filter(_.entityId != ent.entityId ).foreach {pl =>
              pl.session.self ! Session.RelativeLook(ent.entityId, angYaw, angPitch)
          }
        }
        ent.hasRotate = false
        ent.hasMove = false
        ent.previousLocation = ent.location
      }

    case GetChunksForDistance(location, distance) =>
      val pos = location.toPosition

      val centralX = pos.x >> 4
      val centralZ = pos.z >> 4
      println("ceb x", centralX)

      for(x <- centralX - distance to centralZ+distance)
        for(z <- centralZ - distance to centralZ + distance){
          sender() ! getChunk(x, z)
        }

    case JoinPlayer(p) =>
      players.foreach{pl =>
          pl.session.self ! Session.AddNewPlayer(p)
          p.session.self ! Session.AddNewPlayer(pl)
      }
      entities += p
      players += p

    case DisconnectPlayer(p, r) =>
      entities -= p
      players -= p
      players.foreach{pl =>
          pl.session.self ! Session.DisconnectPlayer(p)
      }

    case AnimateEntity(eId, aId) =>
      players.filter(_.entityId != eId).foreach {pl =>
        pl.session.self ! Session.AnimationEntity(eId, aId)
      }

    case UpdateEntityPosition(id, loc) =>
      val entity: Option[Entity] = entities.find(_.entityId == id)
      if(entity.isDefined) {
        val ent = entity.get
        //val relLoc = Utils.locationToRelativeLocation(loc, ent.location)
        ent.location = loc
        ent.hasMove = true
//        val needTp = needTeleport(relLoc.x, relLoc.y, relLoc.z)
//        entities.flatMap{case pl: Player if pl.entityId != id => Some(pl)}.foreach(p => {
//          if (!needTp) p.session.self ! Session.RelativeMove(id, relLoc.x.toShort, relLoc.y.toShort, relLoc.z.toShort)
//          else p.session.self ! Session.TeleportEntity(id, loc)
//        })
        }
    case UpdateEntityPositionAndLook(id, loc) =>
      val entity: Option[Entity] = entities.find(_.entityId == id)
      if(entity.isDefined) {
        val ent = entity.get
        //val relLoc = Utils.locationToRelativeLocation(loc, ent.location)
        //val angYaw = Utils.angleToByte(loc.yaw)
        //val angPitch = Utils.angleToByte(loc.pitch)
        ent.location = loc
        ent.hasRotate = true
        ent.hasMove = true
//        val needTp = needTeleport(relLoc.x, relLoc.y, relLoc.z)
//        entities.flatMap{case pl: Player if pl.entityId != id => Some(pl)}.foreach(p => {
//          if (!needTp) p.session.self ! Session.RelativeMoveAndLook(id, relLoc.x.toShort, relLoc.y.toShort, relLoc.z.toShort, angYaw, angPitch)
//          else p.session.self ! Session.TeleportEntity(id, loc)
//        })
      }
    case UpdateEntityLook(id, loc) =>
      //val angYaw = Utils.angleToByte(yaw)
      //val angPitch = Utils.angleToByte(pitch)
      val entity: Option[Entity] = entities.find(_.entityId == id)
      if(entity.isDefined) {
        val ent = entity.get
        ent.location = loc
        ent.hasRotate = true
        //players.filter(_.uuid != pl.uuid).foreach(_.session.self ! ChangeEntityLook(pl.entityId, angYaw, angPitch))
      }
  }


}