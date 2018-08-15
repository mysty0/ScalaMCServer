package com.scalamc.models

import akka.actor.ActorRef

case class Plugin(mainActor: ActorRef, pluginInfo: PluginInfo)
