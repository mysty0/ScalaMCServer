package com.scalamc.commands.handlers

import java.util.UUID

import akka.actor.{Actor, ActorRef, ActorSelection}
import com.scalamc.actors.EntityController
import com.scalamc.commands.CommandHandlerEvents
import com.scalamc.models.Player
import com.scalamc.models.entity.mob.Zombie

class SummonCommandHandler extends Actor{

  private def getEntityController(world: ActorSelection) = context.actorSelection(world.pathString + "/entityController")

  override def receive: Receive = {
    case CommandHandlerEvents.HandleCommand(sender, command, args) =>
      if(args.length > 1 && command == "summon")
        getEntityController(sender.world) ! EntityController.SummonMobById(args(1), sender.location)
  }
}
