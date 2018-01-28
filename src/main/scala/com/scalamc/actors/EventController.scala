package com.scalamc.actors

import akka.actor.Actor
import com.scalamc.models.Events.JoinPlayerEvent

class EventController extends Actor{

  override def receive ={
    case JoinPlayerEvent =>
  }
}
