package com.scalamc.actors

import akka.actor.{Actor, ActorRef}
import akka.io.Tcp.Write
import akka.util.ByteString
import com.scalamc.objects.ServerStats
import com.scalamc.packets.status.{Handshake, StatusPacket}
import com.scalamc.utils.ByteBuffer
import io.circe.Printer
import io.circe.generic.auto._
import io.circe.syntax._

import scala.collection.mutable.ArrayBuffer

case class SendStat(sender: ActorRef, protocol: Int)

class ServerStatsHandler extends Actor{

  override def receive = {
    case SendStat(s, protocol) => {
      val stats = Printer.noSpaces.copy(dropNullKeys = true).pretty(ServerStats.getStatusWithProtocolId(protocol).asJson)
      implicit val protocolId = protocol
      s ! Write(StatusPacket(stats))

    }
  }
}

