package com.scalamc.packets.game

import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class TimeUpdate(var worldAge: Long = 0, var timeOfDay:Long = 0)
  extends Packet(PacketInfo(0x46.toByte, direction = PacketDirection.Client)){

  def this(){this(0)}
}
