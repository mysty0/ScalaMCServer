package com.scalamc.actors

import akka.actor.{Actor, ActorLogging}
import com.scalamc.actors.PlayersController.JoinPlayer
import com.scalamc.models.Player
import com.scalamc.models.world.World

import scala.collection.mutable.ArrayBuffer

object PlayersController{
  case class JoinPlayer()
}

class PlayersController extends Actor with ActorLogging {

  var players = ArrayBuffer[Player]()

  override def receive = {
    case JoinPlayer =>

      
  }
}
