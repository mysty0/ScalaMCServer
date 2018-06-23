package com.scalamc.models

import java.util.UUID

import com.scalamc.actors.Session
import com.scalamc.models.entity.LivingEntity


case class Player(var name: String, entityId: Int, var uuid: UUID, session: Session, var location: Location) extends LivingEntity{
  override var previousLocation: Location = location
}
