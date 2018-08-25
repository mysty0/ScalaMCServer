package com.scalamc.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.scalamc.actors.PacketProcessor.{ProcessPackets, ProcessedPacket}
import com.scalamc.utils.{ByteBuffer, ByteStack}

object PacketProcessor{
  def props() = Props(
    new PacketProcessor()
  )

  case class ProcessPackets(bytes: ByteStack)
  case class ProcessedPacket(packet: ByteBuffer)
}

class PacketProcessor() extends Actor with ActorLogging{
  override def receive: Receive = {
    case ProcessPackets(bytes) =>
      try {
        while (bytes.nonEmpty && bytes.length > 2){
          sender() ! ProcessedPacket(bytes.popWith(bytes.popVarInt()))
        }
      } catch {
        case e: Exception =>
          log.error("Packet parse error "+e)
      }
  }
}
