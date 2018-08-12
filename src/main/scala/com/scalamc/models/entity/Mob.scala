package com.scalamc.models.entity

import java.util.UUID

import com.scalamc.models.{Location, RawLocation}
import com.scalamc.packets.game.entity.spawn.SpawnMobPacket

trait Mob extends LivingEntity{
  def toSpawnMobPacket(id: Int, uUID: UUID, location: Location) = SpawnMobPacket(id, uUID, typeId, RawLocation(location))
}
