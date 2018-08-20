package com.scalamc.actors

import java.util.UUID

import akka.actor.{Actor, ActorRef, Cancellable, Props}
import com.scalamc.actors.Session.AddNewPlayer
import com.scalamc.actors.World._
import com.scalamc.models.entity.Entity
import com.scalamc.models.enums.BlockFace.BlockFaceVal
import com.scalamc.models.enums.DiggingStatus
import com.scalamc.models.enums.DiggingStatus.DiggingStatusVal
import com.scalamc.models.world.Block
import com.scalamc.models.{Chat, Location, Player, Position}
import com.scalamc.models.world.chunk.generators.FlatChunkGenerator
import com.scalamc.models.world.chunk.{Chunk, ChunkGenerator}
import com.scalamc.packets.Packet
import com.scalamc.packets.game.BlockChangePacket
import com.scalamc.utils.Utils

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._


object World{

  case class GetChunksForDistance(location: Location, distance: Int)

  def props(chunkGenerator: ChunkGenerator = new FlatChunkGenerator()) = Props(
    new World(chunkGenerator)
  )

  private val minChunkUpdateDistance = 10

  private val period = 100.millisecond

  case object Tick
  case class JoinPlayer(player: Player)
  case class DisconnectPlayer(player: Player, reason: Chat)
  case class UpdateEntityPosition(entityId: Int, loc: Location)
  case class UpdateEntityPositionAndLook(entityId: Int, loc: Location)
  case class UpdateEntityLook(entityId: Int, loc: Location)
  case class AnimateEntity(entityId: Int, animationId: Byte)
  case class SendPacketToAllPlayers(packet: Packet)

  case class SetBlock(position: Position, block: Block)

  case class PlayerDigging(player: Player, diggingStatus: DiggingStatusVal, blockPosition: Position, blockFace: BlockFaceVal)
  case class PlayerPlaceBlock(player: Player, position: Position,
                              face: BlockFaceVal,
                              hand: Byte, cursorX: Float,
                              cursorY: Float,
                              cursorZ: Float)
}

class World(chunkGenerator: ChunkGenerator) extends Actor{
  import context._

  val entityController: ActorRef = context.actorOf(EntityController.props(this.self), "entityController")

  var chunks = new mutable.HashMap[Long,Chunk]()
  var entities: ArrayBuffer[Entity] = ArrayBuffer[Entity]()
  var players: ArrayBuffer[Player] = ArrayBuffer[Player]()
  var playersLastChunkUpdateLocation: scala.collection.mutable.Map[UUID, Location] = scala.collection.mutable.Map()
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

  def getRendererRanges(position: Position, distance: Int): (Range, Range) ={
    val centralX = position.x >> 4
    val centralZ = position.z >> 4
    (centralX - distance to centralX + distance, centralZ - distance to centralZ + distance)
  }

  def unloadChunk(x: Int, z: Int, player: Player): Unit ={
    player.session ! Session.UnloadChunk(getChunk(x, z))
  }

  def loadChunk(x: Int, z: Int, player: Player): Unit ={
    player.session ! Session.LoadChunk(getChunk(x, z))
  }

  def updatePlayerChunks(player: Player): Unit ={
    if(playersLastChunkUpdateLocation.contains(player.uuid)){
      val lastLoc = playersLastChunkUpdateLocation(player.uuid)
      if(lastLoc.distanceXZ(player.location) > minChunkUpdateDistance) {
        val lastRanges = getRendererRanges(lastLoc.toPosition, player.settings.viewDistance/2)
        val currRanges = getRendererRanges(player.location.toPosition, player.settings.viewDistance/2)

        def applyToNonOverlappingChunks(firstRanges: (Range, Range), secondRanges: (Range, Range), action: (Int, Int)=>Unit): Unit ={
          for(x <- firstRanges._1)
            for(z <- firstRanges._2)
              if(!(secondRanges._1.contains(x) && secondRanges._2.contains(z)))
                action(x, z)
        }

        applyToNonOverlappingChunks(lastRanges, currRanges, (x, y) => unloadChunk(x, y, player))
        applyToNonOverlappingChunks(currRanges, lastRanges, (x, y) => loadChunk(x, y, player))

        playersLastChunkUpdateLocation(player.uuid) = player.location
      }
    } else
      playersLastChunkUpdateLocation += (player.uuid -> player.location)
  }

  override def receive = {
    case SendPacketToAllPlayers(p) => players.foreach(_.session ! p)

    case SetBlock(pos, block) =>
      getChunk(pos.x >> 4, pos.z >> 4).setBlock(pos.toChunkRelativePosition, block)
      println("set block at "+pos)
      self ! SendPacketToAllPlayers(BlockChangePacket(pos, block))

    case Tick =>
      players.filter(_.hasMove).foreach(updatePlayerChunks)
      players.filter(e => e.hasRotate || e.hasMove).foreach{ent =>
        if(ent.hasMove) {
          val relLoc = Utils.locationToRelativeLocation(ent.location, ent.previousLocation)
          if (ent.hasRotate) {
            val angYaw = Utils.angleToByte(ent.location.yaw)
            val angPitch = Utils.angleToByte(ent.location.pitch)
            players.filter(_.entityId != ent.entityId ).foreach {pl =>
                pl.session ! Session.RelativeMoveAndLook(ent.entityId, relLoc.x.toShort, relLoc.y.toShort, relLoc.z.toShort, angYaw, angPitch)
            }
          }else{
            players.filter(_.entityId != ent.entityId ).foreach {pl =>
                pl.session ! Session.RelativeMove(ent.entityId, relLoc.x.toShort, relLoc.y.toShort, relLoc.z.toShort)
            }
          }
        }
        else if(ent.hasRotate){
          val angYaw = Utils.angleToByte(ent.location.yaw)
          val angPitch = Utils.angleToByte(ent.location.pitch)
          players.filter(_.entityId != ent.entityId ).foreach {pl =>
              pl.session ! Session.RelativeLook(ent.entityId, angYaw, angPitch)
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

      val dist = distance/2

      for(x <- centralX - dist to centralX+dist)
        for(z <- centralZ - dist to centralZ + dist){
          sender() ! getChunk(x, z)
        }

    case JoinPlayer(p) =>
      players.foreach{pl =>
          pl.session ! Session.AddNewPlayer(p)
          p.session ! Session.AddNewPlayer(pl)
      }
      entities += p
      players += p

    case DisconnectPlayer(p, r) =>
      entities -= p
      players -= p
      players.foreach{pl =>
          pl.session ! Session.DisconnectPlayer(p)
      }

    case AnimateEntity(eId, aId) =>
      players.filter(_.entityId != eId).foreach {pl =>
        pl.session ! Session.AnimationEntity(eId, aId)
      }

    case PlayerDigging(player, status, pos, face) =>
      if(status == DiggingStatus.FinishedDigging) getChunk(pos.x >> 4, pos.z >> 4).setBlock(pos.toChunkRelativePosition, Block(0,0))
      players.filter(_.entityId != player.entityId).foreach {pl => pl.session ! BlockChangePacket(pos, Block(0,0))}

    case PlayerPlaceBlock(player, pos, face, hand, cX, cY, cZ) =>
      val block = Block(player.inventory.items(36+player.selectedSlot).id, 0)
      getChunk(pos.x >> 4, pos.z >> 4).setBlock((pos+face.posMod).toChunkRelativePosition, block)//player.inventory.items(player.selectedSlot).id
      players.filter(_.entityId != player.entityId).foreach {pl => pl.session ! BlockChangePacket(pos+face.posMod, block)}

    case UpdateEntityPosition(id, loc) =>
      val entity: Option[Entity] = entities.find(_.entityId == id)
      if(entity.isDefined) {
        val ent = entity.get
        //val relLoc = Utils.locationToRelativeLocation(loc, ent.location)
        ent.location = loc
        ent.hasMove = true
//        val needTp = needTeleport(relLoc.x, relLoc.y, relLoc.z)
//        entities.flatMap{case pl: Player if pl.entityId != id => Some(pl)}.foreach(p => {
//          if (!needTp) p.session ! Session.RelativeMove(id, relLoc.x.toShort, relLoc.y.toShort, relLoc.z.toShort)
//          else p.session ! Session.TeleportEntity(id, loc)
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
//          if (!needTp) p.session ! Session.RelativeMoveAndLook(id, relLoc.x.toShort, relLoc.y.toShort, relLoc.z.toShort, angYaw, angPitch)
//          else p.session ! Session.TeleportEntity(id, loc)
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
        //players.filter(_.uuid != pl.uuid).foreach(_.session ! ChangeEntityLook(pl.entityId, angYaw, angPitch))
      }
  }


}