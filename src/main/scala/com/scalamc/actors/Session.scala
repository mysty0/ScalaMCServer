package com.scalamc.actors

import java.util.UUID.randomUUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp.Write
import com.scalamc.actors.WorldsController.GetDefaultWorld

import scala.concurrent.duration._
import com.scalamc.models.world.{Block, World}
import com.scalamc.models.world.chunk.Chunk
import com.scalamc.models.{Location, Player, Position, Server}
import com.scalamc.packets.game._
import com.scalamc.packets.game.player.{PlayerLookPacket, PlayerPositionAndLookPacketClient, PlayerPositionAndLookPacketServer, PlayerPositionPacket}
import com.scalamc.packets.login.{JoinGamePacket, LoginStartPacket, LoginSuccessPacket}

import scala.concurrent._
import ExecutionContext.Implicits.global



object Session{
  def props(connect: ActorRef) = Props(
    new Session(connect)
  )
}

class Session(connect: ActorRef, var name: String = "") extends Actor with ActorLogging {
  val playersController = context.actorSelection("/user/playersController")
  val worldController = context.actorSelection("/user/worldsController")

  override def receive = {
    case p: LoginStartPacket =>{
      name = p.name
      println("new player connect",name)
      //println(sender())
      //println(ByteString(LoginSuccessPacket(randomUUID().toString, name).toArray))
      //println("uuid len", randomUUID().toString.length)
      connect ! LoginSuccessPacket(randomUUID().toString, name)

      connect ! JoinGamePacket()

      sender() ! ChangeState(ConnectionState.Playing)

      worldController ! GetDefaultWorld

      //connect ! PlayerPositionAndLookPacketClient(0.0, 10.0)

      Server.players += Player(name, this)

      context.system.scheduler.schedule(0 millisecond,30 second) {
        connect ! KeepAliveClientPacket(System.currentTimeMillis())
      }
    }

    case world: World =>
      connect ! PlayerPositionAndLookPacketClient(0.0, 10.0)
      world.getChunksForDistance(Location(0,0,0), 3).foreach(ch => connect ! ch.toPacket(true, true))


    case p: KeepAliveClientPacket =>
      //connect ! Write(KeepAliveServerPacket(p.id))
      //println("recive keep alive")

    case p: PlayerPositionAndLookPacketServer =>
      println("pos and look ", p.x, p.y, p.z, p.yaw, p.pitch)
      //connect ! Write(PositionAndLookPacketClient(p.x, p.y, p.z, p.yaw+10, p.pitch+10))
    case p: PlayerPositionPacket =>
      println("pos ", p.x, p.y, p.z)
     // connect ! Write(PositionAndLookPacketClient(p.x, p.y, p.z, 0, 0))
    case p: PlayerLookPacket =>
      println("look ", p.yaw, p.pitch)
    case p: ClientSettingsPacket =>
      println("setts", p)
    case p: TeleportConfirmPacket =>
  }


}
