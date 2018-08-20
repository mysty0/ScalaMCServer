package com.scalamc.commands.handlers

import akka.actor.Actor
import com.scalamc.actors.World
import com.scalamc.commands.CommandHandlerEvents
import com.scalamc.models.Position
import com.scalamc.models.world.Block

class SetBlockCommandHandler extends Actor{
  override def receive: Receive = {
    case CommandHandlerEvents.HandleCommand(sender, command, args) =>
      if(command == "setblock") sender.world ! World.SetBlock(Position(sender.location.x.toInt, sender.location.y.toInt, sender.location.z.toInt), Block(1,0))
  }
}
