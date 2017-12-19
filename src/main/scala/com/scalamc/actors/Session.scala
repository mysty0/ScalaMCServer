package com.scalamc.actors

import java.util.UUID.randomUUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp.Write
import com.scalamc.packets.game.{KeepAliveClientPacket, KeepAliveServerPacket, SpawnPositionPacket}
import com.scalamc.packets.login.{JoinGamePacket, LoginStartPacket, LoginSuccessPacket}



object Session{
  def props(connect: ActorRef, name: String = "") = Props(
    new Session(connect, name)
  )
}

class Session(connect: ActorRef, var name: String = "") extends Actor with ActorLogging {
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

      connect ! Write(SpawnPositionPacket())
    }
    case p: KeepAliveClientPacket =>
      connect ! Write(KeepAliveServerPacket(p.id))
  }


}
