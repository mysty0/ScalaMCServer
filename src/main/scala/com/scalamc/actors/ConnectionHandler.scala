package com.scalamc.actors

import java.nio.charset.{Charset, StandardCharsets}
import java.util.Base64

import akka.actor.{Actor, ActorRef}
import akka.util.ByteString
import com.scalamc.objects.SessionManager
import com.scalamc.packets.{Packet, PacketState}
import com.scalamc.packets.login.LoginStartPacket
import com.scalamc.utils._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
object ConnectionState extends Enumeration{
  val Login, Playing = Value
}
case class ChangeState(state: ConnectionState.Value)

class ConnectionHandler extends Actor{
    import akka.io.Tcp._

    val starsService = context.actorSelection("/user/stats")

    var session: ActorRef = _

    var state: ConnectionState.Value = ConnectionState.Login

    def receive = {
      case Received(data) => {
        implicit val dataForParser = data
        println("packet ", javax.xml.bind.DatatypeConverter.printHexBinary(data.toArray))
        val stack = new PacketStack(data.toArray)
        stack.handlePackets(parsePacket)

      }
      case cs: ChangeState =>
        println("chage state")
        state = cs.state

      case PeerClosed     => context stop self
    }

  private def parsePacket(packet: ByteBuffer)(implicit data: ByteString) = {
    val packetId = packet(0)
    if (session != null) {
      session ! Packet.fromByteBuffer(packet, if (state==ConnectionState.Login)PacketState.Login else PacketState.Playing)

    } else {


      if (packetId == 0 && packet.last == 1)
        starsService ! SendStat(sender())
      if (packetId == 0 && packet.last == 2) {
        //SessionManager.createSession(sender = sender())
        session = context.actorOf(Session.props(sender()))
      }
      if (packetId == 1 && packet.length > 1) {
        println("pong")
        sender() ! Write(data)
      }
    }
  }
}
