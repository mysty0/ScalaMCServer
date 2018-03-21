package com.scalamc.packets.game.player

import java.util.UUID

import com.scalamc.models.utils.VarInt
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

class SpawnPlayer(var id: VarInt = VarInt(0),
                  var uuid: UUID = UUID.randomUUID(),
                  var x: Double = 0.0,
                  var y: Double = 0.0,
                  var z: Double = 0.0,
                  var yaw: Float = 0.0f,
                  var pitch: Float = 0.0f)
  extends Packet(PacketInfo(Map(-1 -> 0x0B.toByte), direction = PacketDirection.Client)) {
  def this(){this(VarInt(0))}
}
