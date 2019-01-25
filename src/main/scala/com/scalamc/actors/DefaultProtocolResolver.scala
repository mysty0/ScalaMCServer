package com.scalamc.actors

import akka.actor.{Actor, ActorRef, Props}
import com.scalamc.actors.ConnectionHandler.Disconnect
import com.scalamc.actors.Session.TeleportConfirm
import com.scalamc.models.{Location, ProtocolEvents}
import com.scalamc.packets.{Packet, PacketDirection}
import com.scalamc.packets.game.{AnimationPacketClient, AnimationPacketServer, ClientSettingsPacket, KeepAliveClientPacket}
import com.scalamc.packets.game.player._
import com.scalamc.packets.game.player.inventory.SetSlotPacket
import com.scalamc.packets.login.LoginStartPacket

object DefaultProtocolResolver{
  def props(connectionHandler: ActorRef, protocolId: Int) = Props(
    new DefaultProtocolResolver(connectionHandler, protocolId)
  )
}

class DefaultProtocolResolver(connectionHandler: ActorRef, protocolId: Int) extends Actor{
  val session = context.actorOf(Session.props(self))

  override def receive: Receive = resolverEvent orElse localProcess

  def resolverEvent: Receive = localProcess

  protected def localProcess: Receive = {
    case pack: LoginStartPacket =>
      session ! Session.LoginStart(pack.name)
    case pack: KeepAliveClientPacket =>
      session ! Session.KeepAlive(pack.id)
    case pack: PlayerPositionAndLookPacketServer =>
      session ! Session.SetPlayerLocation(Location(pack.x, pack.y, pack.z, pack.yaw, pack.pitch), pack.onGround)
    case pack: PlayerPositionPacket =>
      session ! Session.SetPlayerPosition(pack.x, pack.y, pack.z, pack.onGround)
    case pack: PlayerLookPacket =>
      session ! Session.SetPlayerRotation(pack.yaw, pack.pitch, pack.onGround)
    case pack: AnimationPacketServer =>
      session ! Session.AnimatePlayerHand(pack.hand.int)
    case pack: ClientSettingsPacket =>
      session ! Session.UpdateSettings(pack.locale, pack.viewDistance, pack.chatMode, pack.chatColors, pack.displayedSkinParts, pack.mainHand)
    case pack: TeleportConfirm =>
      session ! Session.TeleportConfirm(pack.id)

    case ProtocolEvents.SetSlotItem(windowId, slot, item) =>
      connectionHandler ! ConnectionHandler.SendPacket(SetSlotPacket(windowId.toByte, slot.toByte, item.toRaw))
      println("set slot"+slot)

    case d: Disconnect =>
      session ! Disconnect()
      println("disconnect support service")
      context stop self
    case p: Packet =>
      p.packetInfo.direction match{
        case PacketDirection.Client => connectionHandler ! ConnectionHandler.SendPacket(p)//client ! Write(p)
        case PacketDirection.Server => session ! p
      }
    case other =>
      connectionHandler ! other
  }
}
