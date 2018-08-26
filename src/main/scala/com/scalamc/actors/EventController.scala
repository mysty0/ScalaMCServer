package com.scalamc.actors

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.scalamc.actors.EventController.{NewPacket, NewWorldEvent}
import com.scalamc.actors.EventTypes.EventType
import com.scalamc.actors.world.World.WorldEvent
import com.scalamc.packets.Packet

object EventTypes extends Enumeration {
  type EventType = Value
  val World, Packet = Value
}

object EventController{

  case class RegisterEventHandler(eventHandler: ActorRef, eventsType: EventType)

  case class NewWorldEvent(event: WorldEvent)
  case class NewPacket(packet: Packet)
}


class EventController extends Actor with ActorLogging{
  var eventHandlers: collection.mutable.Map[EventType, Set[ActorRef]] = collection.mutable.Map()
  override def receive: Receive ={
    case EventController.RegisterEventHandler(handler, eventType) =>
      log.info("new register {} on type {}", handler, eventType)
      if(!eventHandlers.contains(eventType)) eventHandlers += (eventType -> Set())
      eventHandlers(eventType) = eventHandlers(eventType)+handler

    case NewPacket(packet) =>
      eventHandlers getOrElseUpdate (EventTypes.Packet, Set()) foreach {_ ! packet}

    case NewWorldEvent(worldEvent) =>
      eventHandlers getOrElseUpdate (EventTypes.World, Set()) foreach {_ ! worldEvent}
  }
}
