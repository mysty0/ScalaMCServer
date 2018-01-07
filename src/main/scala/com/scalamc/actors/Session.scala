package com.scalamc.actors

import java.util.UUID.randomUUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp.Write
import com.scalamc.models.world.chunk.Chunk
import com.scalamc.models.{Player, Server}
import com.scalamc.packets.game._
import com.scalamc.packets.login.{JoinGamePacket, LoginStartPacket, LoginSuccessPacket}



object Session{
  def props(connect: ActorRef, name: String = "") = Props(
    new Session(connect, name)
  )
}

class Session(connect: ActorRef, var name: String = "", var protocolId: Int = 0) extends Actor with ActorLogging {
  override def receive = {
    case p: LoginStartPacket =>{
      name = p.name
      println(name)
      println(sender())
      //println(ByteString(LoginSuccessPacket(randomUUID().toString, name).toArray))
      println("uuid len", randomUUID().toString.length)
      connect ! Write(LoginSuccessPacket(randomUUID().toString, name))

      connect ! Write(JoinGamePacket())

      sender() ! ChangeState(ConnectionState.Playing)

      var emptChunk = new Chunk(0, 0)
      emptChunk.initEmptyChunk()
      for(x <- 0 until 16)
        for(z <- 0 until 16)
          emptChunk.setType(x, 3, z, 2)
      var packet = emptChunk.toPacket(true, true)
      connect ! Write(packet)

      //connect ! Write(SpawnPositionPacket())

      connect ! Write(PositionAndLookPacketClient())

      Server.players += Player(name, this)
    }
    case p: KeepAliveClientPacket =>
      connect ! Write(KeepAliveServerPacket(p.id))

    case p: PositionAndLookPacketServer =>
      println("pos and look ", p.x, p.y, p.z, p.yaw, p.pitch)
      connect ! Write(PositionAndLookPacketClient(p.x, p.y, p.z, p.yaw+10, p.pitch+10))
    case p: PositionPacket =>
      println("pos ", p.x, p.y, p.z)
      connect ! Write(PositionAndLookPacketClient(p.x, p.y, p.z, 0, 0))
    case p: ClientSettingsPacket =>
      println("setts", p)
    case p: TeleportConfirmPacket =>
  }


}
