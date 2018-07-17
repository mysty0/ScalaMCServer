package com.scalamc.actors

import akka.actor.{Actor, ActorRef, Props}
import com.scalamc.actors.InventoryController.{GetInventory, HandleInventoryPacket, SetInventory, SetSlot}
import com.scalamc.models.inventory.{InventoryItem, PlayerInventory}
import com.scalamc.packets.Packet
import com.scalamc.packets.game.player.inventory.{ClickWindowPacket, CreativeInventoryActionPacket, SetSlotPacket}

object InventoryController{
  def props(session: ActorRef) = Props(
    new InventoryController(session)
  )

  case class SetSlot(slot: Int, item: InventoryItem)
  case class SetInventory(inventory: PlayerInventory)
  case class GetInventory()
  case class HandleInventoryPacket(packet: Packet)
}

class InventoryController(session: ActorRef) extends Actor{
  var inventory: PlayerInventory = new PlayerInventory()

  override def receive: Receive = {
    case SetSlot(slot, item) =>
      inventory.items(slot) = item
      session ! SetSlotPacket(0, slot.toShort, item.toRaw)
    case SetInventory(inv) =>
      inv.items.indices.foreach(i => if(inventory.items(i) != inv.items(i)) session ! SetSlotPacket(0, i.toShort, inv.items(i).toRaw))
      inventory = inv
    case GetInventory =>
      sender ! inventory

    case HandleInventoryPacket(packet) =>
      packet match {
        case p: ClickWindowPacket =>

        case ac: CreativeInventoryActionPacket =>

      }
  }
}
