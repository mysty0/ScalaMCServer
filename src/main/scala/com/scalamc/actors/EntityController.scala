package com.scalamc.actors

import java.util.UUID

import akka.actor.{Actor, ActorRef, Props}
import com.scalamc.actors.EntityController._
import com.scalamc.models.Location
import com.scalamc.models.entity.{Entity, Mob}
import org.reflections.Reflections

import scala.collection.JavaConverters._
import scala.collection.mutable

object EntityController{

  def props(world: ActorRef) = Props(
    new EntityController(world)
  )

  lazy val entities: mutable.Set[Entity] = {

    val reflections = new Reflections("com.scalamc.models.entity")
    val subclasses = reflections.getSubTypesOf(classOf[Entity])

    subclasses.asScala.filter(p=> p.getConstructors.length > 0) map (p=> p.newInstance())
  }

  case class SummonMob(entity: Mob, location: Location)
  case class SummonMobById(id: String, location: Location)
}

class EntityController(world: ActorRef) extends Actor{
  override def receive: Receive = {
    case SummonMob(entity, loc) =>
      world ! World.SendPacketToAllPlayers(entity.toSpawnMobPacket(988, UUID.randomUUID(), loc))

    case SummonMobById(id, loc) =>
      EntityController.entities.foreach(e => println(e.nameId))
      println(EntityController.entities.size)
      world ! World.SendPacketToAllPlayers(EntityController.entities.find(_.nameId == id).get.asInstanceOf[Mob].toSpawnMobPacket(988, UUID.randomUUID(), loc))
  }
}
