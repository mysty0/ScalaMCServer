package com.scalamc.actors


import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.util.ByteString

object CServer {
  def props(port: Int) =
    Props(new CServer(port))
}

class CServer(port:Int) extends Actor with ActorLogging {

  override def preStart() {
    log.info("Starting tcp net server")

    import context.system
    val opts = List(SO.KeepAlive(on = true), SO.TcpNoDelay(on = true))
    IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", port), options = opts)
  }

  //IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", port))

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
