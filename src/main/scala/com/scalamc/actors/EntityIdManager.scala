package com.scalamc.actors

import akka.actor.{Actor, Props}
import com.scalamc.actors.EntityIdManager.GetId

object EntityIdManager{
  def props() = Props(
    new EntityIdManager()
  )
  object GetId
}

class EntityIdManager extends Actor{
  var lastId: Int = 1

  override def receive: PartialFunction[Any, Unit] = {
    case GetId =>
      lastId += 1
      sender() ! lastId
  }
}
