package com.scalamc.models

object Events{
  case class JoinPlayerEvent(player: Player)
  case class Disconnect(player: Player, reason: Chat)
  case class ChangePlayerPosition(player: Player, newLocation: Location, prevLocation: Location)
  case class ChangePlayerPositionAndLook(player: Player, newLocation: Location, prevLocation: Location)
  case class ChagePlayerLook(player: Player, yaw: Float, pitch: Float)
  case class RelativePlayerMove(player: Player, x: Short, y: Short, z: Short)
  case class RelativePlayerMoveAndLook(player: Player, x: Short, y: Short, z: Short, yaw: Byte, pitch: Byte)

  case class ChangeEntityLook(id: Int, yaw: Byte, pitch: Byte)
  case class TeleportEntity(id: Int, location: Location)

  case class GetPlayersPosition(player: Player)
}
