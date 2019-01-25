package com.scalamc.models

import akka.actor.{Actor, ActorRef}
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo, PacketState}

case class PacketInfoJson(var oldId: Int, id: Int, packetDirection: PacketDirection, packetState: PacketState)

case class ProtocolInfo(protocolId: Int = 0,
                        dependProtocol: Int = 0,
                        resolver: String = "",
                        packetsIds: Map[Int, PacketInfo],
                        packets: Map[Int, String])

case class ProtocolVersion(info: ProtocolInfo, resolver: Option[Class[Actor]], packets: Map[Int, Packet]) {

}
