package com.scalamc.actors

import akka.actor.{Actor, ActorRef}
import akka.io.Tcp.Write
import akka.util.ByteString
import com.scalamc.objects.ServerStats
import com.scalamc.utils.{ByteBuffer, VarInt}
import io.circe.generic.auto._
import io.circe.syntax._

import scala.collection.mutable.ArrayBuffer

case class SendStat(sender: ActorRef)

class ServerStatsHandler extends Actor{

 // val status = "{\"version\":{\"name\":\"1.11.2\",\"protocol\":316},\"players\":{\"max\":100,\"online\":0},\"description\":{\"text\":\"Hello world\"}}"
  val stats = ServerStats.serverStats.asJson.noSpaces
  override def receive = {
    case send: SendStat => {
      println("send stat", stats)
      val bStat = stats.getBytes
      var res = new ByteBuffer()
      //res += (2 + bStat.length).toByte
      //res += 0x00.toByte
      //res += bStat.length.toByte
      res.writeWarInt(2 + bStat.length)
      res.writeWarInt(0)
      res.writeWarInt(bStat.length)
      //VarInt.writeVarInt(2 + bStat.length, res)
      //VarInt.writeVarInt(0, res)
      //VarInt.writeVarInt(bStat.length, res)
      res += bStat

      //println(javax.xml.bind.DatatypeConverter.printHexBinary(res.toArray))
      //println(ByteString(res.toArray))

      send.sender ! Write(ByteString(res.toArray))

    }
  }
}

