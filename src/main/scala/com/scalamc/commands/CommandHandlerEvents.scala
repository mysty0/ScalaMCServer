package com.scalamc.commands

import com.scalamc.models.Player

object CommandHandlerEvents {

  case class HandleCommand(sender: Player, command: String, args: Array[String])
  case class HandleTabComplete(sender: Player, leftCommand: String)//: Array[String]
}
