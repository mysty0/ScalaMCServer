package com.scalamc.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.event.LoggingReceive
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

  var pickedItem: Option[InventoryItem] = None

  def dropItem(item: InventoryItem, count: Byte) ={

  }

  def copyItem(item: InventoryItem, count: Int): InventoryItem = new InventoryItem(item.id, item.metadata, count)


  def printInventory(): Unit ={
    for((el, ind) <- inventory.items.view.zipWithIndex)
      println(ind+" "+el)
  }


  override def receive: Receive = LoggingReceive{
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
            case ClickType.Click =>
              pickedItem orElse {
                val item = inventory.items(slot)
                button match {
                  case 0 =>
                    pickedItem = Some(item)
                    inventory.items(slot) = null
                  case 1 if item.count > 1 =>
                    pickedItem = Some(copyItem(inventory.items(slot), count = Math.ceil(item.count/2.0).toByte))
                    inventory.items(slot) = copyItem(inventory.items(slot), count = Math.floor(item.count/2.0).toByte)
                  case 1 if item.count == 1 =>
                    pickedItem = Some(item)
                    inventory.items(slot) = null
                }
                None
              } foreach { item =>
                val itemInSlot = inventory.items(slot)
                button match {
                  case 0 if itemInSlot == null =>
                    inventory.items(slot) = item
                    pickedItem = None
                  case 0 if itemInSlot != item =>
                    inventory.items(slot) = item
                    pickedItem = Some(itemInSlot)
                  case 0 =>
                    inventory.items(slot).count += item.count
                    pickedItem = None
                  case 1 if itemInSlot == null =>
                    inventory.items(slot) = copyItem(item, count = 1)
                    pickedItem = Some(copyItem(item, count = (item.count-1).toByte))
                  case 1 if itemInSlot == item =>
                    inventory.items(slot) = copyItem(item, count = inventory.items(slot).count+1)
                    item.count -= 1
                    //pickedItem = Some(item.copy(count = (item.count-1).toByte))
                }

              }

            case ClickType.MouseDrag =>
              println("mouse drag packet "+packet)

          }
          printInventory()
          println(s"picked item: $pickedItem")

        case CreativeInventoryActionPacket(slot, item) =>
          assert(slot >= 0)
          if(item.itemId < 0) inventory.items(slot) = null
          else inventory.items(slot) = InventoryItem(item)
      }
  }
}
