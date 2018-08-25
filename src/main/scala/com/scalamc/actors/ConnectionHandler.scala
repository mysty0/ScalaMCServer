package com.scalamc.actors

import akka.actor._
import akka.util.{ByteString, Timeout}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global
import com.scalamc.actors.ConnectionHandler._
import com.scalamc.actors.PacketProcessor.ProcessedPacket
import com.scalamc.packets.{Packet, PacketState}
import com.scalamc.packets.status.Handshake
import com.scalamc.utils._


object ConnectionHandler {
  case class HandleStatusPackets()
  case class HandleLoginPackets(protocolId: Int)
  case class HandlePlayPackets()
  case class SendPacket(packet: Packet)
  case class Disconnect()
}

class ConnectionHandler extends Actor with ActorLogging with Stash {
  import akka.io.Tcp._

  implicit val timeout: Timeout = Timeout(5 seconds)

  var client: Option[ActorRef] = None

  val handshakeHandler: ActorRef = context.actorOf(HandshakeHandler.props(), "handshakeHandler")
  val packetProcessor: ActorRef = context.actorOf(PacketProcessor.props(), "packetProcessor")

  var session: Option[ActorRef] = None

  implicit var protocolId: Int = 0

  def receive: Receive = handlePackets orElse status

  def handlePackets: Receive = {
    case Received(data) =>
      if(client.isEmpty) client = Some(sender())
      log.info("process packets: {}", javax.xml.bind.DatatypeConverter.printHexBinary(data.toArray))
      packetProcessor ! PacketProcessor.ProcessPackets(new ByteStack(data.toArray))


    case SendPacket(pack) =>
      client foreach {_ ! Write(pack)}

    case PeerClosed =>
      session foreach {_ ! Disconnect()}
      context stop self
  }

  def status: Receive = {

    case PacketProcessor.ProcessedPacket(pack) =>
      log.info("process status packet: {}", javax.xml.bind.DatatypeConverter.printHexBinary(pack.toArray))
      handshakeHandler ! Packet.fromByteBuffer(pack, PacketState.Status)
      context.become(waitLogin)
  }

  def waitLogin: Receive = {
    case HandleLoginPackets(protId) =>
      protocolId = protId
      session = Some(context.actorOf(MultiVersionSupportService.props(self, protocolId), "MultiVersionSupportService"))
      context.unbecome()
      context.unbecome()
      context.become(handlePackets orElse login)
      unstashAll()

    case _: HandleStatusPackets =>
      context.unbecome()
      unstashAll()

    case other =>
      stash()
  }

  def login: Receive = {

    case PacketProcessor.ProcessedPacket(pack) =>
      log.info("process login packet: {}", javax.xml.bind.DatatypeConverter.printHexBinary(pack.toArray))
      session foreach {_ ! Packet.fromByteBuffer(pack, PacketState.Login)}

    case _:HandlePlayPackets =>
      context.unbecome()
      context.become(handlePackets orElse play)
      //context.become(play)

    case other => log.info("recive login other "+other)
  }

  def play: Receive = {
    case PacketProcessor.ProcessedPacket(pack) =>
      log.info("process play packet: {}", javax.xml.bind.DatatypeConverter.printHexBinary(pack.toArray))
      session foreach {_ ! Packet.fromByteBuffer(pack, PacketState.Playing)}
    //if(session.isDefined) session.get ! Packet.fromByteBuffer(pack, PacketState.Playing)

  }

}
