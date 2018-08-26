package com.scalamc.actors

import java.util.UUID.randomUUID

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.scalamc.ScalaMC
import com.scalamc.actors.ConnectionHandler.Disconnect
import com.scalamc.actors.Session._
import com.scalamc.actors.world.World

import scala.concurrent.duration._
import com.scalamc.models.world.Block
import com.scalamc.models.world.chunk.Chunk
import com.scalamc.models._
import com.scalamc.models.enums.BlockFace.BlockFaceVal
import com.scalamc.models.enums.{BlockFace, DiggingStatus, GameMode}
import com.scalamc.models.enums.GameMode.GameModeVal
import com.scalamc.models.inventory.InventoryItem
import com.scalamc.models.utils.VarInt
import com.scalamc.packets.Packet
import com.scalamc.packets.game._
import com.scalamc.packets.game.entity._
import com.scalamc.packets.game.player._
import com.scalamc.packets.game.player.inventory.{ClickWindowPacket, CreativeInventoryActionPacket, SetSlotPacket, SlotRaw}
import com.scalamc.packets.login.{JoinGamePacket, LoginStartPacket, LoginSuccessPacket}
import com.scalamc.utils.Utils
import io.circe.Printer
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

object Session{
  def props(connect: ActorRef) = Props(
    new Session(connect)
  )

  case class SendPacketToConnect(packet: Packet)

  case class DisconnectSession(reason: Chat)
  case class DisconnectPlayer(player: Player)
  case class AddNewPlayer(player: Player)
  case class RelativeMove(entityId: Int, x: Short, y: Short, z: Short)
  case class RelativeMoveAndLook(entityId: Int, x: Short, y: Short, z: Short, yaw: Byte, pitch: Byte)
  case class RelativeLook(entityId: Int, yaw: Byte, pitch: Byte)
  case class TeleportEntity(entityId: Int, location: Location)
  case class AnimationEntity(entityId: Int, animationId: Byte)
  case class LoadChunk(chunk: Chunk)
  case class UnloadChunk(x: Int, z: Int)
}

class Session(connect: ActorRef) extends Actor with ActorLogging {

  val world: ActorSelection = context.actorSelection("/user/defaultWorld")
  val chatHandler: ActorSelection = context.actorSelection("/user/chatHandler")
  val eventController: ActorSelection = context.actorSelection("/user/eventController")

  val inventoryController: ActorRef = context.actorOf(InventoryController.props(self))

  var player: Player = _

  var timeUpdateSchedule: Cancellable = _

  implicit val timeout: Timeout = Timeout(5 seconds)

  def addNewPlayer(pl: Player): Unit ={
    var actions = ArrayBuffer[PlayerItem]()
    actions += PlayerItem(uuid = pl.uuid, action = AddPlayerListAction(name = pl.name))
    connect ! PlayerListItemPacket(actions = actions)
    connect ! SpawnPlayerPacket(VarInt(pl.entityId), pl.uuid, pl.location.x, pl.location.y, pl.location.z, pl.location.yaw.toByte, pl.location.pitch.toByte)
  }


  override def receive: PartialFunction[Any, Unit] = {
    case p: LoginStartPacket =>
      val future = ScalaMC.entityIdManager ? EntityIdManager.GetId
      Await.result(future, timeout.duration) match{
        case id: Int =>
          player = Player(p.name, id, randomUUID(), this.self, ScalaMC.worldController, Location(0, 65, 0))
          //      if(Players.players.contains(player)){
          //        self ! DisconnectSession(Chat("You already playing on this server"))
          //      }
          //Players.players += player
          log.info("new player connect",p.name, player.uuid, player.entityId)
          connect ! LoginSuccessPacket(player.uuid.toString, p.name)
          connect ! JoinGamePacket(0, GameMode.Survival)
          //connect ! PluginMessagePacketServer("MC|Brand", "name".getBytes("UTF-8"))
          connect ! ConnectionHandler.HandlePlayPackets()
          connect ! PlayerPositionAndLookPacketClient(0.0, 65.0)
          world ! World.JoinPlayer(player)
          inventoryController ! InventoryController.SetSlot(44, new InventoryItem(1, count = 120))
          timeUpdateSchedule = context.system.scheduler.schedule(0 millisecond,10 second) {
            connect ! TimeUpdate(0,9999);//KeepAliveClientPacket(System.currentTimeMillis())
            //world ! GetPlayersPosition(player)
          }
      }

    case InventoryController.UpdateInventory(inv) =>
      player.inventory = inv

    case chunk: Chunk =>
      connect ! chunk.toPacket(skylight = true, entireChunk = true)

    case LoadChunk(chunk) =>
      connect ! chunk.toPacket(skylight = true, entireChunk = true)

    case UnloadChunk(x, z) =>
      connect ! UnloadChunkPacket(x, z)

    case ChatMessagePacket(msg)=>
      chatHandler ! ChatHandler.NewMessage(msg, player)
    case TabCompleteRequestPacket(command, assumeCommand, blockPosition) =>
      chatHandler ! ChatHandler.TabCompleteRequest(command, assumeCommand, player)
    case p: KeepAliveClientPacket =>
      //connect ! Write(KeepAliveServerPacket(p.id))
      println("recive keep alive",p.id)

    case p: PlayerPositionAndLookPacketServer =>
      world ! World.UpdateEntityPositionAndLook(player.entityId, Location(p.x, p.y, p.z, p.yaw, p.pitch))
    case p: PlayerPositionPacket =>
      world ! World.UpdateEntityPosition(player.entityId, Location(p.x, p.y, p.z, player.location.yaw, player.location.pitch))

    case p: PlayerLookPacket =>
      world ! World.UpdateEntityLook(player.entityId, Location(player.location.x, player.location.y, player.location.z, p.yaw, p.pitch))

    case AnimationPacketServer(hand) =>
      world ! World.AnimateEntity(player.entityId, if(hand.int == 0) 0 else 3)
    case AnimationEntity(eId, aId) =>
      connect ! AnimationPacketClient(eId, aId)

    case p: ClientSettingsPacket =>
      player.settings = new PlayerSettings(p)
      world ! World.LoadFirstChunks(player)
    case p: TeleportConfirmPacket =>

    case RelativeMove(id, x, y, z) =>
      connect ! EntityRelativeMovePacket(id, x, y, z)
    case RelativeMoveAndLook(id, x, y, z, yaw, pitch) =>
      connect ! EntityRelativeMoveAndLookPacket(id, x, y, z, yaw, pitch)
      connect ! EntityHeadRotationPacket(id, yaw)
    case RelativeLook(id, yaw, pitch) =>
      connect ! EntityLookPacket(id, yaw, pitch)
      connect ! EntityHeadRotationPacket(id, yaw)

    case TeleportEntity(id, loc) =>
      connect ! EntityTeleportPacket(id, loc.x, loc.y, loc.z, Utils.angleToByte(loc.yaw), Utils.angleToByte(loc.pitch), false)

    case AddNewPlayer(pl) =>
      addNewPlayer(pl)

    case action: CreativeInventoryActionPacket =>
      inventoryController ! InventoryController.HandleInventoryPacket(action)

    case click: ClickWindowPacket =>
      inventoryController ! InventoryController.HandleInventoryPacket(click)

    case p: PlayerDiggingPacket =>
      world ! World.PlayerDigging(player, DiggingStatus(p.status.int), p.position, BlockFace(p.face).asInstanceOf[BlockFaceVal])

    case p: BlockPlacePacket =>
      world ! World.PlayerPlaceBlock(player, p.position, BlockFace(p.face.int).asInstanceOf[BlockFaceVal], p.hand.int.toByte, p.cursorPositionX, p.cursorPositionY, p.cursorPositionZ)

    case HeldItemChangePacketServer(slot) =>
      player.selectedSlot = slot.toByte

    case DisconnectSession(reason) =>
        val printer = Printer.noSpaces.copy(dropNullKeys = true)
        self ! Disconnect()
        connect ! DisconnectPacket(printer.pretty(reason.asJson))
        connect ! Disconnect()
        context stop self
    case DisconnectPlayer(pl) =>
        var actions = ArrayBuffer[PlayerItem]()
        actions += PlayerItem(uuid = pl.uuid, action = RemovePlayerListAction())
        connect ! PlayerListItemPacket(action = VarInt(4), actions = actions)
        var ids = new ArrayBuffer[VarInt]()
        ids += pl.entityId
        connect ! DestroyEntitiesPacket(ids)

    case d: Disconnect =>
      world ! World.DisconnectPlayer(player, Chat())
      Players.players -= player
      timeUpdateSchedule.cancel()
      context stop self

    case SendPacketToConnect(packet) =>
      connect ! packet
  }


}
