package com.scalamc.actors


import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.util.ByteString

object CServer {
  def props(ip: String, port: Int) =
    Props(new CServer(ip, port))
}

class CServer(ip: String, port:Int) extends Actor with ActorLogging {

  override def preStart() {
    log.info("Starting tcp net server on "+ip+":"+port.toString)

    import context.system
    val opts = List(SO.KeepAlive(on = true), SO.TcpNoDelay(on = true))
    IO(Tcp) ! Bind(self, new InetSocketAddress(ip, port), options = opts)
  }

  def receive = {
    case b @ Bound(localAddress) =>
      context.parent ! b

    case CommandFailed(_: Bind) => context stop self

    case c @ Connected(remote, local) =>
      val handler = context.actorOf(Props[ConnectionHandler])
      val connection = sender()

      connection ! Register(handler)
  }
}
