package com.scalamc.packets.status

import com.scalamc.models.VarInt
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo, PacketState}

case class Handshake(var protocolVersion: VarInt = VarInt(0),
                     var serverAddress: String = "",
                     var serverPort:Short = 0,
                     var nextState:VarInt = VarInt(0))
  extends Packet(PacketInfo(Map(-1 -> 0x00.toByte), PacketState.Status, PacketDirection.Server)){

  def this(){this(VarInt(0))}
}
