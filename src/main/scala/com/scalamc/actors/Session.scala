package com.scalamc.actors

import java.util.UUID.randomUUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp.Write
import com.scalamc.actors.ConnectionHandler.{ChangeState, Disconnect}
import com.scalamc.actors.World.GetChunksForDistance
import com.scalamc.models.Events.ChangePosition

import scala.concurrent.duration._
import com.scalamc.models.world.Block
import com.scalamc.models.world.chunk.Chunk
import com.scalamc.models._
import com.scalamc.packets.game._
import com.scalamc.packets.game.player.{PlayerLookPacket, PlayerPositionAndLookPacketClient, PlayerPositionAndLookPacketServer, PlayerPositionPacket}
import com.scalamc.packets.login.{JoinGamePacket, LoginStartPacket, LoginSuccessPacket}
import io.circe.Printer
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.collection.mutable.ArrayBuffer



object Session{
  def props(connect: ActorRef) = Props(
    new Session(connect)
  )
}

class Session(connect: ActorRef) extends Actor with ActorLogging {

  val world = context.actorSelection("/user/defaultWorld")
  val eventController = context.actorSelection("/user/eventController")

  override def receive = {
    case p: LoginStartPacket =>{

      var pl = Player(p.name, randomUUID(), this)
      if(Players.players.contains(pl)){
        self ! com.scalamc.models.Events.Disconnect(Chat("You already playing on this server"))
      }


      Players.players += pl

      println("new player connect",p.name)
      //println(sender())
      //println(ByteString(LoginSuccessPacket(randomUUID().toString, name).toArray))
      //println("uuid len", randomUUID().toString.length)
      connect ! LoginSuccessPacket(pl.uuid.toString, p.name)

      connect ! JoinGamePacket()

      //connect ! PluginMessagePacketServer("MC|Brand", "name".getBytes("UTF-8"))

      sender() ! ChangeState(ConnectionState.Playing)

      world ! GetChunksForDistance(Location(0,0,0), 3)
      connect ! PlayerPositionAndLookPacketClient(0.0, 10.0)



      context.system.scheduler.schedule(0 millisecond,1 second) {
        connect ! KeepAliveClientPacket(System.currentTimeMillis())
      }
    }

//    case world: ActorRef =>
//      connect ! PlayerPositionAndLookPacketClient(0.0, 10.0)
//      world ! GetChunksForDistance(Location(0,0,0), 3)
//      this.world = world

    case chunks: ArrayBuffer[Chunk] =>
      println("send chunks")
      chunks.foreach(ch => connect ! ch.toPacket(true, true))
      //connect ! PlayerPositionAndLookPacketClient(0.0, 10.0)

    case p: KeepAliveClientPacket =>
      //connect ! Write(KeepAliveServerPacket(p.id))
      println("recive keep alive",p.id)

    case p: PlayerPositionAndLookPacketServer =>
      println("pos and look ", p.x, p.y, p.z, p.yaw, p.pitch)
      //connect ! Write(PositionAndLookPacketClient(p.x, p.y, p.z, p.yaw+10, p.pitch+10))
    case p: PlayerPositionPacket =>
      println("pos ", p.x, p.y, p.z)
     // connect ! Write(PositionAndLookPacketClient(p.x, p.y, p.z, 0, 0))
      world ! ChangePosition(Location(p.x, p.y, p.z))
    case p: PlayerLookPacket =>
      println("look ", p.yaw, p.pitch)
    case p: ClientSettingsPacket =>
      println("setts", p)
    case p: TeleportConfirmPacket =>

    case com.scalamc.models.Events.Disconnect(reason) =>
      val printer = Printer.noSpaces.copy(dropNullKeys = true)
      connect ! DisconnectPacket(printer.pretty(reason.asJson))
      connect ! Disconnect()
      context stop self

    case d: Disconnect =>
      println("disconnect session")
      context stop self
  }


}
