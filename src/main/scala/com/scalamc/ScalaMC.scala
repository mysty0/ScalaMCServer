package com.scalamc

import akka.actor.{ActorSystem, Props}
import com.scalamc.actors._
import com.scalamc.actors.world.World
import com.scalamc.actors.world.generators.FlatGenerator


/**
  * Created by MapLegend on 13.06.2017.
  */
object ScalaMC extends App{
  implicit val actorSystem = ActorSystem()
  val port = Option(System.getenv("PORT")).map(_.toInt).getOrElse(25565)
  val mainActor = actorSystem.actorOf(CServer.props("localhost", port))
 // val statsActor = actorSystem.actorOf(Props[ServerStatsHandler], "stats")
  val eventController = actorSystem.actorOf(Props[EventController], "eventController")
  val worldController = actorSystem.actorOf(World.props(actorSystem.actorOf(Props(classOf[FlatGenerator]))), "defaultWorld")
  val chatHandler = actorSystem.actorOf(ChatHandler.props(), "chatHandler")
  val commandHandler = actorSystem.actorOf(CommandsHandler.props(), "commandHandler")
  val entityIdManager = actorSystem.actorOf(EntityIdManager.props(), "entityIdManager")

  val pluginController = actorSystem.actorOf(PluginController.props(), "pluginController")
  pluginController ! PluginController.LoadPluginsFromDir("plugins")

}
