package com.scalamc.actors

import java.io.File
import java.net.URI

import akka.actor.{Actor, Props}
import com.scalamc.models.{Plugin, PluginInfo}
import com.scalamc.utils.ModulesLoader
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

import scala.collection.mutable.ArrayBuffer
import scala.io.Source


object PluginLoader{
  def props() = Props(
    new PluginLoader()
  )

  case class LoadPlugins(directory: String)
}

class PluginLoader extends Actor{
  import com.scalamc.actors.PluginLoader._
  override def receive: Receive = {
    case LoadPlugins(dir) =>
      val classLoader = ModulesLoader.getClassLoader(dir)//new java.net.URLClassLoader(getListOfFiles(dir).map(_.toURL).toArray, this.getClass.getClassLoader)
      val pluginInfos = classLoader.findResources("plugin.info")

      var plugins: ArrayBuffer[Plugin] = ArrayBuffer()
      while(pluginInfos.hasMoreElements){
        val info = decode[PluginInfo](scala.io.Source.fromInputStream(pluginInfos.nextElement().openStream()).mkString).toOption
        if(info.isDefined){
          val inf = info.get
          if(inf.pathToMainClass.nonEmpty)
            plugins += Plugin(context.actorOf(Props(classLoader.loadClass(inf.pathToMainClass)), inf.name), inf)
        }
      }
      sender ! PluginController.AddPlugins(plugins)
  }
}
