package com.scalamc.packets.game

import com.scalamc.models.VarInt
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class ClientSettingsPacket(var locale: String = "",
                                var viewDistance: Byte = 0,
                                var chatMode: VarInt = VarInt(0),
                                var chatColors: Boolean = true,
                                var displayedSkinParts: Byte = 0,
                                var mainHand: VarInt = VarInt(0))
  extends Packet(PacketInfo(0x04.toByte, direction = PacketDirection.Server)){
  def this(){this("")}
}
