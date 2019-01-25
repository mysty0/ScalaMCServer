package com.scalamc.packets.game.entity.spawn

import com.scalamc.models.utils.VarInt
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

case class SpawnExperienceOrbPacket(var entityId: VarInt = 0,
                                    var x: Double = 0,
                                    var y: Double = 0,
                                    var z: Double = 0,
                                    var count: Short = 0)
  extends Packet(PacketInfo(0x01.toByte, direction = PacketDirection.Client)) {

  def this(){this(0)}
}
