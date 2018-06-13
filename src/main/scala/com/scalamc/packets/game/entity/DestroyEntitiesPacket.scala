package com.scalamc.packets.game.entity

import com.scalamc.models.utils.VarInt
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

import scala.collection.mutable.ArrayBuffer

case class DestroyEntitiesPacket(var entityIds: ArrayBuffer[VarInt] = new ArrayBuffer[VarInt]())
  extends Packet(PacketInfo(Map(-1 -> 0x31.toByte), direction = PacketDirection.Client)){
  def this(){this( new ArrayBuffer[VarInt]())}
}
