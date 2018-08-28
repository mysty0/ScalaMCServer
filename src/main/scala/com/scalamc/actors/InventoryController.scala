package com.scalamc.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.scalamc.actors.InventoryController._
import com.scalamc.models.enums.ClickType
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

  case class UpdateInventory(inventory: PlayerInventory)
}

class InventoryController(session: ActorRef) extends Actor with ActorLogging{
  var inventory: PlayerInventory = new PlayerInventory()

  var currentItem: Option[InventoryItem] = None

  def dropItem(item: InventoryItem, count: Byte) ={

  }

  override def receive: Receive = {
    case SetSlot(slot, item) =>
      inventory.items(slot) = item
      session ! Session.SendPacketToConnect(SetSlotPacket(0, slot.toShort, item.toRaw))
      session ! UpdateInventory(inventory)
    case SetInventory(inv) =>
      inv.items.indices.foreach(i => if(inventory.items(i) != inv.items(i)) session ! Session.SendPacketToConnect(SetSlotPacket(0, i.toShort, inv.items(i).toRaw)))
      inventory = inv
      session ! UpdateInventory(inventory)
    case GetInventory =>
      sender ! inventory

    case HandleInventoryPacket(packet) =>
      packet match {
        case ClickWindowPacket(windowId, slot, button, actionNumber, mode, clickedItem) =>
          ClickType(mode.int) match {
            case ClickType.QUICK_MOVE if slot == -999 =>
              currentItem foreach { item =>
                if(actionNumber == 0) dropItem(item, 64)
                if(actionNumber == 1) dropItem(item, 1)
              }
            case ClickType.QUICK_MOVE =>

          }

        case CreativeInventoryActionPacket(slot, item) =>
          assert(slot >= 0)
          if(item.itemId < 0) inventory.items(slot) = null
          else inventory.items(slot) = InventoryItem(item)
      }
  }
}
