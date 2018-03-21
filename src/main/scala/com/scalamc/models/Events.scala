package com.scalamc.models

object Events{
  case class JoinPlayerEvent(player: Player)
  case class Disconnect(reason: Chat)
  case class ChangePosition(location: Location)
}
