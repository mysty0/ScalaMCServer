package com.scalamc.actors

import java.util.UUID

import akka.actor.{Actor, ActorRef, Props}
import com.scalamc.actors.CommandsHandler._
import com.scalamc.commands.CommandHandlerEvents
import com.scalamc.models.entity.mob.Zombie
import com.scalamc.models.{Location, Player, RawLocation}
import com.scalamc.packets.game.entity.spawn.SpawnMobPacket
import org.reflections.Reflections

import scala.collection.JavaConverters._
import scala.collection.mutable

object CommandsHandler{
  def props() = Props(
    new CommandsHandler()
  )

  lazy val commandHandlers: mutable.Set[Class[_ <: Actor]] = {

    val reflections = new Reflections("com.scalamc.commands.handlers")
    val subclasses = reflections.getSubTypesOf(classOf[Actor])

    subclasses.asScala
  }

  case class CommandExecute(command: String, sender: Player)
  case class CommandCompleteRequest(command: String, player: Player)
}

class CommandsHandler() extends Actor {

  lazy val commandHandlers: mutable.Set[ActorRef] = CommandsHandler.commandHandlers.map(c => context.actorOf(Props(c)))


  override def receive: Receive = {
    case CommandExecute(com, player) =>
      val args = com.replace("/", "").split(" ")
      val command = args(0)

      commandHandlers.foreach(_ ! CommandHandlerEvents.HandleCommand(player, command, args))
      //CommandsHandler.commandHandlers.find(_.command==command).getOrElse(new UnknownCommandHandler()).handleCommand(player, command, args)

//      command match {
//        case "summon" =>
//          player.session.self ! Zombie().toSpawnMobPacket(988, UUID.randomUUID(), player.location)
//      }
    case CommandCompleteRequest(com, player) =>

  }
}
