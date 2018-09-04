package com.scalamc.packets.game.player.inventory

import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class ConfirmTransactionClientPacket(var windowId: Byte = 0,
                                    var actionNumber: Short = 0,
                                    var accepted: Boolean = true)
  extends Packet(PacketInfo(Map(-1 -> 0x06.toByte), direction = PacketDirection.Server)){
  def this(){this(0)}
}
case class ConfirmTransactionServerPacket(var windowId: Byte = 0,
                                          var actionNumber: Short = 0,
                                          var accepted: Boolean = true)
  extends Packet(PacketInfo(Map(340 -> 0x12.toByte, 335 -> 0x11.toByte), direction = PacketDirection.Client)){
  def this(){this(0)}
}
