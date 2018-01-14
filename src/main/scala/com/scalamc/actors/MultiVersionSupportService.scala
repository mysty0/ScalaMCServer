package com.scalamc.actors

import akka.actor.{Actor, ActorRef, Props}
import akka.io.Tcp.Write
import com.scalamc.models.VarInt
import com.scalamc.packets.{Packet, PacketDirection}
import com.scalamc.packets.game.{KeepAliveClientPacket, KeepAliveClientPacketOld, KeepAliveServerPacket, KeepAliveServerPacketOld}

object MultiVersionSupportService{
  def props(client: ActorRef, connectionHandler: ActorRef, protocolId: Int = 0) = Props(
    new MultiVersionSupportService(client, connectionHandler, protocolId = protocolId)
  )
}

class MultiVersionSupportService(client: ActorRef, connectionHandler: ActorRef, implicit var protocolId: Int) extends Actor{

  val session = context.actorOf(Session.props(self))

  override def receive = {
    case p: KeepAliveClientPacketOld =>
    case p: KeepAliveClientPacket if protocolId >= 339 => client ! Write(p)
    case p: KeepAliveClientPacket => client ! Write(KeepAliveServerPacketOld(VarInt(p.id.toInt)))

    case p: KeepAliveServerPacket if protocolId >= 339 => session ! p
    case p: KeepAliveServerPacketOld => client ! KeepAliveServerPacket(p.id.int.toLong)

    case p: Packet =>
      println("succ match")
      p.packetInfo.direction match{
        case PacketDirection.Client => client ! Write(p)
        case PacketDirection.Server => session ! p
      }
    case other => connectionHandler ! other
  }
}
