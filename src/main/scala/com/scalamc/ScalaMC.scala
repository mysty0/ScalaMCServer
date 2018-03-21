package com.scalamc

import akka.actor.{ActorSystem, Props}
import com.scalamc.actors._


/**
  * Created by MapLegend on 13.06.2017.
  */
object ScalaMC extends App{
  implicit val actorSystem = ActorSystem()
  val port = Option(System.getenv("PORT")).map(_.toInt).getOrElse(25565)
  val mainActor = actorSystem.actorOf(CServer.props("192.168.1.143", port))
  val statsActor = actorSystem.actorOf(Props[ServerStatsHandler], "stats")
  val eventController = actorSystem.actorOf(Props[EventController], "eventController")
  val worldsController = actorSystem.actorOf(World.props(), "defaultWorld")
}
