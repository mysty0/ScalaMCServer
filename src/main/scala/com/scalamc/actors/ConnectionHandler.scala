package com.scalamc.actors

import akka.actor._
import akka.util.{ByteString, Timeout}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global

import com.scalamc.actors.ConnectionHandler.{ChangeState, Disconnect, HandleLogin}
import com.scalamc.packets.{Packet, PacketState}
import com.scalamc.packets.status.Handshake
import com.scalamc.utils._

object ConnectionState extends Enumeration{
  val Login, Playing, Status = Value

  implicit def connState2PacketState(connState: ConnectionState.Value): PacketState.Value = connState match{
    case Login => PacketState.Login
    case Status => PacketState.Status
    case Playing => PacketState.Playing
  }
}

object ConnectionHandler {
  case class ChangeState(state: ConnectionState.Value)
  case class HandleLogin(protocolId: Int)
  case class Disconnect()
}

class ConnectionHandler extends Actor {
  import akka.io.Tcp._

  implicit val timeout: Timeout = Timeout(5 seconds)

  lazy val statsService: ActorRef = context.actorOf(ServerStatsHandler.props(sender()))

  var session: ActorRef = _

  var state: ConnectionState.Value = ConnectionState.Status

  implicit var protocolId: Int = 0

  def receive = {
    case Received(data) =>
      implicit val dataForParser: ByteString = data
      val stack = new PacketStack(data.toArray)
      stack.handlePackets(parsePacket)


    case cs: ChangeState =>
      println("chage state", protocolId)
      state = cs.state

    case PeerClosed =>
      if(session != null) session ! Disconnect()
      println("disconnect handler", session, protocolId)
      context stop self
  }

  private def parsePacket(packet: ByteBuffer)(implicit data: ByteString): Unit = {
    println("packet ", javax.xml.bind.DatatypeConverter.printHexBinary(data.toArray))
    if (state == ConnectionState.Login || state == ConnectionState.Playing) {
      println("packet id", packet(0))
      session ! Packet.fromByteBuffer(packet, state)
    } else {
      val future = statsService ? Packet.fromByteBuffer(packet, state)
      Await.result(future, timeout.duration) match{
        case HandleLogin(protId) =>
          protocolId = protId
          state = ConnectionState.Login
          println(state)
          session = context.actorOf(MultiVersionSupportService.props(sender(), self, protocolId))
        case other =>
      }
    }
  }

}
