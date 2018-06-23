package com.scalamc.actors

import java.util.UUID.randomUUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp.Write
import com.scalamc.actors.ConnectionHandler.{ChangeState, Disconnect}
import com.scalamc.actors.Session._
import com.scalamc.actors.World.GetChunksForDistance

import scala.concurrent.duration._
import com.scalamc.models.world.Block
import com.scalamc.models.world.chunk.Chunk
import com.scalamc.models._
import com.scalamc.models.utils.VarInt
import com.scalamc.packets.game._
import com.scalamc.packets.game.entity._
import com.scalamc.packets.game.player._
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

  case class DisconnectSession(reason: Chat)
  case class DisconnectPlayer(player: Player)
  case class AddNewPlayer(player: Player)
  case class RelativeMove(entityId: Int, x: Short, y: Short, z: Short)
  case class RelativeMoveAndLook(entityId: Int, x: Short, y: Short, z: Short, yaw: Byte, pitch: Byte)
  case class RelativeLook(entityId: Int, yaw: Byte, pitch: Byte)
  case class TeleportEntity(entityId: Int, location: Location)
}

class Session(connect: ActorRef) extends Actor with ActorLogging {

  val world = context.actorSelection("/user/defaultWorld")
  val eventController = context.actorSelection("/user/eventController")

  var player: Player = _

  def addNewPlayer(pl: Player): Unit ={
    var actions = ArrayBuffer[PlayerItem]()
    actions += PlayerItem(uuid = pl.uuid, action = AddPlayerListAction(name = pl.name))
    connect ! PlayerListItemPacket(actions = actions)
    connect ! SpawnPlayerPacket(VarInt(pl.entityId), pl.uuid, pl.location.x, pl.location.y, pl.location.z, pl.location.yaw.toByte, pl.location.pitch.toByte)

    println("add new player")
  }

  override def receive = {
    case p: LoginStartPacket =>{

      player = Player(p.name, (Players.players.size+100)*10, randomUUID(), this, Location(0, 65, 0))
//      if(Players.players.contains(player)){
//        self ! DisconnectSession(Chat("You already playing on this server"))
//      }


      Players.players += player

      println("new player connect",p.name, player.uuid, player.entityId)
      //println(sender())
      //println(ByteString(LoginSuccessPacket(randomUUID().toString, name).toArray))
      //println("uuid len", randomUUID().toString.length)
      connect ! LoginSuccessPacket(player.uuid.toString, p.name)

      connect ! JoinGamePacket()

      //connect ! PluginMessagePacketServer("MC|Brand", "name".getBytes("UTF-8"))

      sender() ! ChangeState(ConnectionState.Playing)

      world ! World.GetChunksForDistance(Location(0,0,0), 3)
      
      connect ! PlayerPositionAndLookPacketClient(0.0, 65.0)
//filter(_!=pl).
      world ! World.JoinPlayer(player)
      //player.spawn(world.asInstanceOf[World])

      context.system.scheduler.schedule(0 millisecond,10 second) {
        connect ! TimeUpdate(0,9999);//KeepAliveClientPacket(System.currentTimeMillis())
        //world ! GetPlayersPosition(player)
      }
    }

//    case world: ActorRef =>
//      connect ! PlayerPositionAndLookPacketClient(0.0, 10.0)
//      world ! GetChunksForDistance(Location(0,0,0), 3)
//      this.world = world

    case chunk: Chunk =>
      connect ! chunk.toPacket(true, true)


    case p: KeepAliveClientPacket =>
      //connect ! Write(KeepAliveServerPacket(p.id))
      println("recive keep alive",p.id)

    case p: PlayerPositionAndLookPacketServer =>
      println("pos and look ", p.x, p.y, p.z, p.yaw, p.pitch)
      //connect ! Write(PositionAndLookPacketClient(p.x, p.y, p.z, p.yaw+10, p.pitch+10))
      world ! World.UpdateEntityPositionAndLook(player.entityId, Location(p.x, p.y, p.z, p.yaw, p.pitch))
    case p: PlayerPositionPacket =>
     // connect ! Write(PositionAndLookPacketClient(p.x, p.y, p.z, 0, 0))
      world ! World.UpdateEntityPosition(player.entityId, Location(p.x, p.y, p.z, player.location.yaw, player.location.pitch))

    case p: PlayerLookPacket =>
      world ! World.UpdateEntityLook(player.entityId, Location(player.location.x, player.location.y, player.location.z, p.yaw, p.pitch))
    case p: ClientSettingsPacket =>
      println("setts", p)
    case p: TeleportConfirmPacket =>

    case RelativeMove(id, x, y, z) =>
      connect ! EntityRelativeMovePacket(id, x, y, z)
    case RelativeMoveAndLook(id, x, y, z, yaw, pitch) =>
      connect ! EntityRelativeMoveAndLookPacket(id, x, y, z, yaw, pitch)
      //connect ! EntityHeadRotationPacket(pl.entityId, yaw)
    case RelativeLook(id, yaw, pitch) =>
      connect ! EntityLookPacket(id, yaw, pitch)
      //connect ! EntityHeadRotationPacket(id, yaw)

    case TeleportEntity(id, loc) =>
      connect ! EntityTeleportPacket(id, loc.x, loc.y, loc.z, Utils.angleToByte(loc.yaw), Utils.angleToByte(loc.pitch), false)

    case AddNewPlayer(pl) =>
      addNewPlayer(pl)

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
      println("disconnect session")
      context stop self
  }


}
