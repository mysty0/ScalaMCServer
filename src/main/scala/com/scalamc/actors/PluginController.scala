package com.scalamc.actors

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.scalamc.models.Plugin

import scala.collection.mutable.ArrayBuffer

object PluginController{
  def props() = Props(
    new PluginController()
  )

  case class LoadPluginsFromDir(directory: String)
  case class AddPlugins(plugins: ArrayBuffer[Plugin])
}

class PluginController extends Actor with ActorLogging{
  import com.scalamc.actors.PluginController._

  var plugins: ArrayBuffer[Plugin] = ArrayBuffer()
  val pluginLoader: ActorRef = context.actorOf(PluginLoader.props())

  override def receive: Receive = {
    case LoadPluginsFromDir(dir) =>
      log.debug(s"start loading from $dir")
      pluginLoader ! PluginLoader.LoadPlugins(dir)

    case AddPlugins(pls) =>
      log.info("Load plugins complete, load {} plugins",pls.length)
      pls(0).mainActor ! "something"
      plugins.appendAll(pls)
  }
}
