package com.scalamc.actors.world

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.scalamc.ScalaMC
import com.scalamc.actors.world.generators.GeneratorsMessages
import com.scalamc.actors.{EntityController, EventController, Session}
import com.scalamc.models.entity.Entity
import com.scalamc.models.enums.BlockFace.BlockFaceVal
import com.scalamc.models.enums.DiggingStatus
import com.scalamc.models.enums.DiggingStatus.DiggingStatusVal
import com.scalamc.models.world.{Block, WorldInfo}
import com.scalamc.models.world.chunk.Chunk
import com.scalamc.models.{Chat, Location, Player, Position}
import com.scalamc.packets.Packet
import com.scalamc.packets.game.BlockChangePacket
import com.scalamc.packets.game.player.PlayerPositionAndLookPacketClient
import com.scalamc.packets.login.JoinGamePacket
import com.scalamc.utils.Utils

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.Try

object World{

  def props(chunkGenerator: ActorRef, worldInfo: WorldInfo = WorldInfo()) = Props(
    new World(chunkGenerator, worldInfo)
  )

  private val minChunkUpdateDistance = 10

  private val period = 100.millisecond
  private val timeout = 5.second

  trait WorldEvent

  case object Tick                                                    extends WorldEvent
  case class JoinPlayer(player: Player)                               extends WorldEvent
  case class LoadFirstChunks(player: Player)                          extends WorldEvent
  case class DisconnectPlayer(player: Player, reason: Chat)           extends WorldEvent
  case class UpdateEntityPosition(entityId: Int, loc: Location)       extends WorldEvent
  case class UpdateEntityPositionAndLook(entityId: Int, loc: Location)extends WorldEvent
  case class UpdateEntityLook(entityId: Int, loc: Location)           extends WorldEvent
  case class AnimateEntity(entityId: Int, animationId: Byte)          extends WorldEvent
  case class SendPacketToAllPlayers(packet: Packet)                   extends WorldEvent

  case class SetBlock(position: Position, block: Block)               extends WorldEvent

  case class PlayerDigging(player: Player, diggingStatus: DiggingStatusVal, blockPosition: Position, blockFace: BlockFaceVal) extends WorldEvent
  case class PlayerPlaceBlock(player: Player, position: Position,
                              face: BlockFaceVal,
                              hand: Byte, cursorX: Float,
                              cursorY: Float,
                              cursorZ: Float) extends WorldEvent
}

class World(chunkGenerator: ActorRef, worldInfo: WorldInfo) extends Actor with ActorLogging{
  import World._

  implicit val ec = ExecutionContext.global

  val entityController: ActorRef = context.actorOf(EntityController.props(this.self), "entityController")

  var chunks = new mutable.HashMap[Long,Chunk]()
  var entities: ArrayBuffer[Entity] = ArrayBuffer[Entity]()
  var players: ArrayBuffer[Player] = ArrayBuffer[Player]()
  var playersLastChunkUpdateLocation: scala.collection.mutable.Map[UUID, Location] = scala.collection.mutable.Map()
  private var scheduler: Cancellable = _

  var spawnLocation: Location = Location(0, 66)

  override def preStart(): Unit = {
    scheduler = context.system.scheduler.schedule(period, period, self, Tick)
  }

  def getChunk(x: Int, z: Int): Future[Chunk] = {
    val key = (x.toLong << 32) | (z & 0xffffffffL)
    if(!chunks.contains(key)) {
      implicit val timeout: Timeout = World.timeout
      return chunkGenerator ? GeneratorsMessages.GenerateChunk(x, z) flatMap {
        f => Future{
          val newChunk = f.asInstanceOf[GeneratorsMessages.GeneratedChunk].chunk
          chunks += key -> newChunk
          newChunk
        }
      }
    }
    //chunks(key)
    Future{chunks(key)}
  }
  def setChunk(x: Int, z: Int, chunk: Chunk) = chunks((x.toLong << 32) | (z & 0xffffffffL)) = chunk

  def needTeleport(dx: Double, dy: Double, dz: Double): Boolean = dx > Short.MaxValue || dy > Short.MaxValue || dz > Short.MaxValue || dx < Short.MinValue || dy < Short.MinValue || dz < Short.MinValue

  def getRendererRanges(position: Position, distance: Int): (Range, Range) ={
    val centralX = position.x >> 4
    val centralZ = position.z >> 4
    (centralX - distance to centralX + distance, centralZ - distance to centralZ + distance)
  }

  def unloadChunk(x: Int, z: Int, player: Player): Unit ={
    player.session ! Session.UnloadChunk(x, z)
  }

  def loadChunk(x: Int, z: Int, player: Player): Unit ={
    getChunk(x, z) foreach {player.session ! Session.LoadChunk(_)}
  }

  def updatePlayerChunks(player: Player): Unit ={
    if(playersLastChunkUpdateLocation.contains(player.uuid)){
      val lastLoc = playersLastChunkUpdateLocation(player.uuid)
      if(lastLoc.distanceXZ(player.location) > minChunkUpdateDistance) {
        player.settings foreach { plSettings =>
          val lastRanges = getRendererRanges(lastLoc.toPosition, plSettings.viewDistance/2)
          val currRanges = getRendererRanges(player.location.toPosition, plSettings.viewDistance/2)

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

      }
    } else
      playersLastChunkUpdateLocation += (player.uuid -> player.location)
  }

  override def receive: Receive ={
    case some =>
      processEvent(some)
      sendEvents(some)
  }

  def sendEvents: Receive ={
    case event: WorldEvent => ScalaMC.eventController ! EventController.NewWorldEvent(event)
    case _ =>
  }

  def processEvent: Receive = {
    case SendPacketToAllPlayers(p) => players.foreach(_.session ! Session.SendPacketToConnect(p))

    case SetBlock(pos, block) =>
      getChunk(pos.x >> 4, pos.z >> 4) foreach {_.setBlock(pos.toChunkRelativePosition, block)}
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

    case LoadFirstChunks(player) =>
      val pos = player.location.toPosition

      val centralX = pos.x >> 4
      val centralZ = pos.z >> 4

      player.settings foreach {plSettings =>
        val dist = plSettings.viewDistance/2

        for(x <- centralX - dist to centralX+dist)
          for(z <- centralZ - dist to centralZ + dist){
            //sender() ! getChunk(x, z)
            loadChunk(x, z, player)
          }
      }
    case JoinPlayer(p) =>
      players.foreach{pl =>
          pl.session ! Session.AddNewPlayer(p)
          p.session ! Session.AddNewPlayer(pl)
      }
      entities += p
      players += p

      //self ! LoadFirstChunks(p)
      p.session ! Session.SendPacketToConnect(
        JoinGamePacket(
          gamemode = p.gameMode,
          dimension = worldInfo.dimension,
          difficulty = worldInfo.difficulty,
          levelType = worldInfo.levelType
        )
      )
      p.session ! Session.SendPacketToConnect(
        PlayerPositionAndLookPacketClient(
          spawnLocation.x,
          spawnLocation.y,
          spawnLocation.z,
          spawnLocation.yaw,
          spawnLocation.pitch
        )
      )

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
      if(status == DiggingStatus.FinishedDigging) getChunk(pos.x >> 4, pos.z >> 4) foreach {_.setBlock(pos.toChunkRelativePosition, Block(0,0))}
      players.filter(_.entityId != player.entityId).foreach {pl => pl.session ! BlockChangePacket(pos, Block(0,0))}

    case PlayerPlaceBlock(player, pos, face, hand, cX, cY, cZ) =>
      val block = Block(player.inventory.items(36+player.selectedSlot).id, 0)
      getChunk(pos.x >> 4, pos.z >> 4) foreach {_.setBlock((pos+face.posMod).toChunkRelativePosition, block)}//player.inventory.items(player.selectedSlot).id
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