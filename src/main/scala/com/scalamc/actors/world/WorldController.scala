package com.scalamc.actors.world

import akka.actor.{Actor, ActorRef, Props}
import com.scalamc.actors.world.WorldController.{CreateWorld, JoinNewPlayerOnWorld}
import com.scalamc.actors.world.generators.FlatGenerator
import com.scalamc.models.Player

object WorldController{
  def props() = Props(
    new WorldController()
  )

  case class CreateWorld(name: Option[String]= None, chunkGenerator: Option[ActorRef] = None)
  case class JoinNewPlayerOnWorld(worldName: String, player: Player)
}

class WorldController extends Actor{
  var worlds: Map[String, ActorRef] = Map()
  override def receive: Receive = {
    case CreateWorld(n, cg) =>
      val name = n.getOrElse(s"World ${worlds.size+1}")
      val chunkGenerator = cg.getOrElse(context.actorOf(Props[FlatGenerator]))
      worlds += name -> context.actorOf(World.props(chunkGenerator), name)

    case JoinNewPlayerOnWorld(name, player) =>

  }
}
