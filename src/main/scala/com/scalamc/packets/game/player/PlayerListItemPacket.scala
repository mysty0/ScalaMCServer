package com.scalamc.packets.game.player

import java.util.UUID

import com.scalamc.models.utils.VarInt
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}

import scala.collection.mutable.ArrayBuffer

case class PlayerListItemPacket(var action: VarInt = VarInt(0),
                                var actions: ArrayBuffer[PlayerItem] = new ArrayBuffer[PlayerItem]())
  extends Packet(PacketInfo(0x2D.toByte, direction = PacketDirection.Client)){
  def this(){this(VarInt(0))}
}

case class PlayerItem(var uuid: UUID = UUID.randomUUID(),
                      var action: PlayerListAction = AddPlayerListAction()){ def this(){this(UUID.randomUUID())}}

abstract class PlayerListAction()

case class AddPlayerListAction(var name: String = "player",
                               var properties: ArrayBuffer[AddPlayerListActionProperty] = new ArrayBuffer[AddPlayerListActionProperty](),
                               var gamemode: VarInt = VarInt(1),
                               var ping: VarInt = VarInt(3),
                               var displayName: Option[String] = None) extends PlayerListAction{
  def this(){this("")}
}

case class AddPlayerListActionProperty(var name: String = "",
                                       var value: String = "",
                                       var signature: Option[String] = None){
  def this(){this("")}
}

case class RemovePlayerListAction() extends PlayerListAction