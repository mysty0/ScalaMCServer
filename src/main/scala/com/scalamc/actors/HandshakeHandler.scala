package com.scalamc.actors

import akka.actor.{Actor, ActorRef, Props}
import akka.io.Tcp.Write
import akka.util.ByteString
import com.scalamc.objects.ServerStats
import com.scalamc.packets.status.{Handshake, PingPacket, PongPacket, StatusPacket}
import com.scalamc.packets.Packet._
import com.scalamc.utils.ByteBuffer
import io.circe.Printer
import io.circe.generic.auto._
import io.circe.syntax._

import scala.collection.mutable.ArrayBuffer

object HandshakeHandler{
  def props() = Props(
    new HandshakeHandler()
  )

}

class HandshakeHandler() extends Actor{
  var protocolId: Option[Int] = None

  override def receive = {
    case pack: Handshake =>
      protocolId = Some(pack.protocolVersion.int)
      implicit val protId = pack.protocolVersion.int
      pack.nextState.int match {
        case 1 =>
          val stats = Printer.noSpaces.copy(dropNullKeys = true).pretty(ServerStats.getStatus.asJson)
          sender() ! ConnectionHandler.HandleStatusPackets()
          sender() ! ConnectionHandler.SendPacket(StatusPacket(status = stats))
        case 2 =>
          sender() ! ConnectionHandler.HandleLoginPackets(protId)
          context stop self
      }
    case pack: PingPacket =>
      sender() ! ConnectionHandler.HandleStatusPackets()
      sender() ! ConnectionHandler.SendPacket(PongPacket(pack.payload))
  }
}

