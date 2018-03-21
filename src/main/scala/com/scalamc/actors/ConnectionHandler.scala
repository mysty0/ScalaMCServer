package com.scalamc.actors

import akka.actor.{Actor, ActorRef}
import akka.util.ByteString
import com.scalamc.actors.ConnectionHandler.{ChangeState, Disconnect}
import com.scalamc.packets.{Packet, PacketState}
import com.scalamc.packets.status.Handshake
import com.scalamc.utils._

object ConnectionState extends Enumeration{
  val Login, Playing = Value
}

object ConnectionHandler {
  case class ChangeState(state: ConnectionState.Value)
  case class Disconnect()
}

class ConnectionHandler extends Actor {
  import akka.io.Tcp._

  val statsService = context.actorSelection("/user/stats")

  var session: ActorRef = _

  var state: ConnectionState.Value = ConnectionState.Login

  implicit var protocolId = 0

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

    case PeerClosed =>
      if(session != null) session ! Disconnect()
      println("disconnect handler", session, protocolId)
      context stop self
  }

  private def parsePacket(packet: ByteBuffer)(implicit data: ByteString) = {
    val packetId = packet(0)
    if (session != null) {
      println("packet id", packetId)
      session ! Packet.fromByteBuffer(packet, if (state == ConnectionState.Login) PacketState.Login else PacketState.Playing)
    } else {
      println("packet ", javax.xml.bind.DatatypeConverter.printHexBinary(packet.toArray))

      if (packetId == 0 && packet.length>1) {
        val pack = Packet.fromByteBuffer(packet, PacketState.Status).asInstanceOf[Handshake]
        protocolId = pack.protocolVersion.int
        if (pack.nextState.int == 1)
          statsService ! SendStat(sender(), protocolId)
        if (pack.nextState.int == 2)
          session = context.actorOf(MultiVersionSupportService.props(sender(), self, protocolId))
      }
      if (packetId == 1 && packet.length > 1)
        sender() ! Write(data)
    }
  }

}
