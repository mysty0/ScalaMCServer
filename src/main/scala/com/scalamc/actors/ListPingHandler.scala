package com.scalamc.actors

import akka.actor.Actor
import akka.io.Tcp.Received

class ListPingHandler extends Actor{
  def receive = {
    case Received(data) => {
    }
  }
}
