package com.scalamc.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp.Write
import com.scalamc.actors.ConnectionHandler.Disconnect
import com.scalamc.models.utils.VarInt
import com.scalamc.packets.{Packet, PacketDirection}
import com.scalamc.packets.game.{KeepAliveClientPacket, KeepAliveClientPacketOld, KeepAliveServerPacket, KeepAliveServerPacketOld}

object MultiVersionSupportService{
  def props(connectionHandler: ActorRef, protocolId: Int = 0) = Props(
    new MultiVersionSupportService(connectionHandler, protocolId = protocolId)
  )
}

class MultiVersionSupportService(connectionHandler: ActorRef, implicit var protocolId: Int) extends Actor with ActorLogging{

  val session = context.actorOf(Session.props(self), "session")

  override def receive = {
    case p: KeepAliveServerPacket if protocolId >= 339 => ConnectionHandler.SendPacket(p)
    case p: KeepAliveServerPacket => ConnectionHandler.SendPacket(KeepAliveServerPacketOld(VarInt(p.id.toInt)))

    case p: KeepAliveClientPacket if protocolId >= 339 => session ! p
    case p: KeepAliveClientPacketOld => ConnectionHandler.SendPacket(KeepAliveServerPacket(p.id.int.toLong))

    case d: Disconnect =>
      session ! Disconnect()
      //client ! Disconnect()
      println("disconnect support service")
      context stop self

    case p: Packet =>
      p.packetInfo.direction match{
        case PacketDirection.Client => connectionHandler ! ConnectionHandler.SendPacket(p)//client ! Write(p)
        case PacketDirection.Server => session ! p
      }
    case other =>
      connectionHandler ! other
  }

}
