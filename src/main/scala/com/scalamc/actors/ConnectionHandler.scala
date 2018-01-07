package com.scalamc.actors

import java.nio.charset.{Charset, StandardCharsets}
import java.util.Base64

import akka.actor.{Actor, ActorRef}
import akka.util.ByteString
import com.scalamc.objects.SessionManager
import com.scalamc.packets.{Packet, PacketState}
import com.scalamc.packets.login.LoginStartPacket
import com.scalamc.packets.status.Handshake
import com.scalamc.utils._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
object ConnectionState extends Enumeration{
  val Login, Playing = Value
}
case class ChangeState(state: ConnectionState.Value)

class ConnectionHandler extends Actor {

  import akka.io.Tcp._

  val statsService = context.actorSelection("/user/stats")

  var session: ActorRef = _

  var state: ConnectionState.Value = ConnectionState.Login

  var protocolId = 0

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

    case PeerClosed => context stop self
  }

  private def parsePacket(packet: ByteBuffer)(implicit data: ByteString) = {
    val packetId = packet(0)
    if (session != null) {
      println("packet id", packetId)
      session ! Packet.fromByteBuffer(packet, if (state == ConnectionState.Login) PacketState.Login else PacketState.Playing)

    } else {
      println("packet ", javax.xml.bind.DatatypeConverter.printHexBinary(packet.toArray))

      if (packetId == 0 && packet.last == 1)
        statsService ! SendStat(sender(), Packet.fromByteBuffer(packet, PacketState.Status).asInstanceOf[Handshake].protocolVersion.int)
      if (packetId == 0 && packet.last == 2) {
        protocolId = Packet.fromByteBuffer(packet, PacketState.Status).asInstanceOf[Handshake].protocolVersion.int
        session = context.actorOf(Session.props(sender()))
      }

      if (packetId == 1 && packet.length > 1) {
        println("pong")
        sender() ! Write(data)
      }
    }
  }

}
