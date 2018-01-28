package com.scalamc.actors

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.scalamc.actors.WorldsController._

import scala.collection.mutable.ArrayBuffer

object WorldsController{
  case class CreateNewWorld()
  case class GetDefaultWorld()
}

class WorldsController  extends Actor with ActorLogging {
  var worlds = ArrayBuffer[ActorRef](context.actorOf(World.props()))

  override def receive =  {
    case CreateNewWorld =>
      worlds += context.actorOf(World.props())

    case GetDefaultWorld =>
      println("get world request")
      sender() ! worlds(0)
    }

}
