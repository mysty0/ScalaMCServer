package com.scalamc.packets.game.playerslist

import com.scalamc.models.utils.VarInt
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

import scala.collection.mutable.ArrayBuffer

case class PlayerListPacket(var action: VarInt = VarInt(0),
                            var numbersOfPlayers: VarInt = VarInt(0),
                            var players: ArrayBuffer[PlayerListAction] = ArrayBuffer())
  extends Packet(PacketInfo(Map(-1 -> 0x2E.toByte), direction = PacketDirection.Client)){
  def this(){this(VarInt(0))}
}
