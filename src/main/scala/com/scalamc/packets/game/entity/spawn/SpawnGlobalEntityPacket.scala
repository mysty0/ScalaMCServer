package com.scalamc.packets.game.entity.spawn

import com.scalamc.models.utils.VarInt
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class SpawnGlobalEntityPacket(var entityId: VarInt = 0,
                                   var entityType: Byte = 0,
                                   var x: Double = 0,
                                   var y: Double = 0,
                                   var z: Double = 0)
  extends Packet(PacketInfo(0x02.toByte, direction = PacketDirection.Client)) {

  def this(){this(0)}

}
