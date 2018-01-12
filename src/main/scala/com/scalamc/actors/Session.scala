package com.scalamc.actors

import java.util.UUID.randomUUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp.Write
import com.scalamc.models.world.Block
import com.scalamc.models.world.chunk.Chunk
import com.scalamc.models.{Player, Position, Server}
import com.scalamc.packets.game._
import com.scalamc.packets.game.player.{PlayerLookPacket, PlayerPositionAndLookPacketClient, PlayerPositionAndLookPacketServer, PlayerPositionPacket}
import com.scalamc.packets.login.{JoinGamePacket, LoginStartPacket, LoginSuccessPacket}



object Session{
  def props(connect: ActorRef, protocolId: Int = 0) = Props(
    new Session(connect, protocolId = protocolId)
  )
}

class Session(connect: ActorRef, var name: String = "", implicit var protocolId: Int = 0) extends Actor with ActorLogging {
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
//      emptChunk.setBlock(0, 2, 0, Block(22, 0))
      //emptChunk.setBlock(0, 20, 0, Block(5, 0))
      emptChunk.setBlock(0, 0, 0, Block(5, 0))
      for(x <- 0 until 16)
        for(z <- 0 until 16)
          emptChunk.setBlock(x, 2, z, Block(1, 0))

      var packet = emptChunk.toPacket(true, true)
      connect ! Write(packet)

      connect ! Write(BlockChangePacket(Position(0, 4, 0), Block(5, 0.toByte)))
//      for(x <- 0 until 16)
//        for(z <- 0 until 16)
//          connect ! Write(BlockChangePacket(Position(x, 2, z), Block(5, 0.toByte)))

      //connect ! Write(SpawnPositionPacket())

      connect ! Write(PlayerPositionAndLookPacketClient(0.0, 10.0))

      Server.players += Player(name, this)
    }
    case p: KeepAliveClientPacket =>
      connect ! Write(KeepAliveServerPacket(p.id))

    case p: KeepAliveClientPacket335 =>
      connect ! Write(KeepAliveServerPacket335(p.id))

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
