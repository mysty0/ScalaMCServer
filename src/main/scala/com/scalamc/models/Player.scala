package com.scalamc.models

import java.util.UUID

import com.scalamc.actors.Session
import com.scalamc.packets.game.player.PlayerPositionAndLookPacketClient


case class Player(var name: String, var entityId: Int, var uuid: UUID, var session: Session, var position: Location) {
  def teleport(newPos: Location): Unit ={
    position = newPos
    session.self ! PlayerPositionAndLookPacketClient()
  }
}
