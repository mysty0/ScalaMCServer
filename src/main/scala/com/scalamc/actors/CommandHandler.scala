package com.scalamc.actors

import java.util.UUID

import akka.actor.{Actor, Props}
import com.scalamc.actors.CommandHandler._
import com.scalamc.models.entity.mob.Zombie
import com.scalamc.models.{Location, Player, RawLocation}
import com.scalamc.packets.game.entity.spawn.SpawnMobPacket

object CommandHandler{
  def props() = Props(
    new CommandHandler()
  )
  case class CommandExecute(command: String, sender: Player)
  case class CommandCompleteRequest(command: String, player: Player)
}

class CommandHandler() extends Actor{
  override def receive: Receive = {
    case CommandExecute(com, player) =>
      val args = com.replace("/", "").split(" ")
      val command = args(0)

      command match {
        case "summon" =>
          player.session.self ! Zombie().toSpawnMobPacket(988, UUID.randomUUID(), player.location)
      }
    case CommandCompleteRequest(com, player) =>

  }
}
