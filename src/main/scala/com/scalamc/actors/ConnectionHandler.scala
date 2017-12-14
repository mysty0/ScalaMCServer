package com.scalamc.actors

import java.nio.charset.{Charset, StandardCharsets}
import java.util.Base64

import akka.actor.{Actor, ActorRef}
import akka.util.ByteString
import com.scalamc.models.Session
import com.scalamc.objects.SessionManager
import com.scalamc.packets.{Packet, PacketState}
import com.scalamc.packets.login.LoginStartPacket
import com.scalamc.utils.{ByteBuffer, VarInt, VarIntJ}

import scala.collection.mutable.ArrayBuffer
object ConnectionState extends Enumeration{
  val Login, Playing = Value
}
class ConnectionHandler extends Actor{
    import akka.io.Tcp._

    val starsService = context.actorSelection("/user/stats")

    var session: ActorRef = _

    var state: ConnectionState.Value = ConnectionState.Login

    def receive = {
      case Received(data) => {
        //val byteData = data.asByteBuffer
        //println(byteData)
       // println(data.decodeString(Charset.defaultCharset()))
        val b = new ByteBuffer(data.toArray)
        //byteData.get(b)

        println("packet ",javax.xml.bind.DatatypeConverter.printHexBinary(b.toArray))
        //val r1 = Array(0x22)
        //val r2 = Array(0x11)
        //println(javax.xml.bind.DatatypeConverter.printHexBinary(r1++r2))
        var start = 0
        while(start < b.length-1 && b(start)>0) {
          //println(byteData.get(start))
          val packet = new Array[Byte](b.readVarInt(start) + 1)
          println(b.readVarInt(start))
          Array.copy(b.toArray, start + 1, packet, 0, b.readVarInt(start) + 1) //b.copyToArray(packet, 1, byteData.get(start))
          println(javax.xml.bind.DatatypeConverter.printHexBinary(packet))
          println("id ", packet(0))
          println("len ", packet.length)
          println("last ", packet.last)
          println("start", start)

          if (session != null) {
            if(state==ConnectionState.Login){
              session ! Packet.fromByteBuffer(new ByteBuffer(packet), PacketState.Login)
            }
          } else {
            if (packet(0) == 0 && packet.last == 1)
              starsService ! SendStat(sender())
            if (packet(0) == 0 && packet.last == 2) {
              //SessionManager.createSession(sender = sender())
              session = context.actorOf(Session.props(sender))
            }
            if (packet(0) == 1 && packet.length > 1) {
              println("pong")
              sender() ! Write(data)
            }

          }
          start = start + b.readVarInt(start) + 2
        }
      }
      case PeerClosed     => context stop self
    }
}
