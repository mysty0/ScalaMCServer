package com.scalamc.objects

import akka.actor.{ActorContext, ActorRef}
import com.scalamc.models.{Session}

import scala.collection.mutable.ArrayBuffer

object SessionManager {

  //var sessions = ArrayBuffer[ActorRef]()

  def createSession(sender: ActorRef)(implicit context: ActorContext) = context.actorOf(Session.props(sender))

//  def hasSession(sender: ActorRef) = {
//    for (session <- sessions)
//      if(session.sender.equals(sender))
//        true
//    false
//  }
//
//  def getSession(sender: ActorRef) = sessions.find((s: Session) => s.sender.equals(sender)).get
}
