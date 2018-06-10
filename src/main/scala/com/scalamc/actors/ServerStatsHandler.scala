package com.scalamc.actors

import akka.actor.{Actor, ActorRef, Props}
import akka.io.Tcp.Write
import akka.util.ByteString
import com.scalamc.actors.ConnectionHandler.HandleLogin
import com.scalamc.objects.ServerStats
import com.scalamc.packets.status.{Handshake, PingPacket, PongPacket, StatusPacket}
import com.scalamc.packets.Packet._
import com.scalamc.utils.ByteBuffer
import io.circe.Printer
import io.circe.generic.auto._
import io.circe.syntax._

import scala.collection.mutable.ArrayBuffer

object ServerStatsHandler{
  def props(client: ActorRef) = Props(
    new ServerStatsHandler(client)
  )
}

class ServerStatsHandler(client: ActorRef) extends Actor{
  implicit var protocolId = 0

  override def receive = {

    case pack: Handshake =>
      protocolId = pack.protocolVersion.int
      pack.nextState.int match {
        case 1 =>
          val stats = Printer.noSpaces.copy(dropNullKeys = true).pretty(ServerStats.getStatus.asJson)
          client ! Write(StatusPacket(stats))
          sender() ! Unit
        case 2 =>
          sender() ! HandleLogin(protocolId)
          context stop self
      }
    case pack: PingPacket =>
      client ! Write(PongPacket(pack.payload))
      sender() ! Unit
  }
}

