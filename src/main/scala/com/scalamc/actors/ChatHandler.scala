package com.scalamc.actors

import akka.actor.{Actor, ActorRef, ActorSelection, Props}
import com.scalamc.actors.ChatHandler._
import com.scalamc.models.{Chat, Player}

object ChatHandler{
  def props() = Props(
    new ChatHandler()
  )

  val commandChar: Char = '/'

  case class NewMessage(message: String, sender: Player)
  case class TabCompleteRequest(text: String, assumeCommand: Boolean, sender: Player)
}

class ChatHandler extends Actor{
  val commandHandler: ActorSelection = context.actorSelection("/user/commandHandler")

  override def receive: Receive = {
    case NewMessage(msg, sender) =>
      println(s"${sender.name} send: $msg")
      if(msg.head == commandChar)
        commandHandler ! CommandsHandler.CommandExecute(msg, sender)
    case TabCompleteRequest(text, isCommand, sender) =>
      commandHandler ! CommandsHandler.CommandCompleteRequest(text, sender)
  }
}
