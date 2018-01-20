package com.scalamc.actors

import akka.actor.{Actor, ActorLogging}
import com.scalamc.actors.WorldsController._
import com.scalamc.models.world.World

import scala.collection.mutable.ArrayBuffer

object WorldsController{
  case class CreateNewWorld()
  case class GetDefaultWorld()
}

class WorldsController  extends Actor with ActorLogging {
  var worlds = ArrayBuffer[World](new World())

  override def receive =  {
    case CreateNewWorld =>
      worlds += new World()

    case GetDefaultWorld =>
      println("get world request")
      sender() ! worlds(0)
    }

}
