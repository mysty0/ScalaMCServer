package com.scalamc.models

object Events {
  trait Event

  case class PlayerMove(player: Player) extends Event
  case class PlayerChatMessage() extends Event
}
