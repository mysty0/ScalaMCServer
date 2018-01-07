package com.scalamc.actors

import akka.actor.{Actor, ActorRef}
import akka.io.Tcp.Write
import akka.util.ByteString
import com.scalamc.objects.ServerStats
import com.scalamc.packets.status.{Handshake, StatusPacket}
import com.scalamc.utils.ByteBuffer
import io.circe.generic.auto._
import io.circe.syntax._

import scala.collection.mutable.ArrayBuffer

case class SendStat(sender: ActorRef, protocol: Int)

class ServerStatsHandler extends Actor{

 // val status = "{\"version\":{\"name\":\"1.11.2\",\"protocol\":316},\"players\":{\"max\":100,\"online\":0},\"description\":{\"text\":\"Hello world\"}}"

  override def receive = {
    case SendStat(s, protocol) => {
      val stats = ServerStats.getStatusWithProtocolId(protocol).asJson.noSpaces
//      println("send stat", stats)
//      val bStat = stats.getBytes
//      var res = new ByteBuffer()
//      res.writeVarInt(2 + bStat.length)
//      res.writeVarInt(0)
//      res.writeVarInt(bStat.length)
//      res += bStat

      //println(javax.xml.bind.DatatypeConverter.printHexBinary(res.toArray))
      //println(ByteString(res.toArray))

      s ! Write(StatusPacket(stats))

    }
  }
}

