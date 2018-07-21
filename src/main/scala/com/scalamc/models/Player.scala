package com.scalamc.models

import java.util.UUID

import com.scalamc.actors.Session
import com.scalamc.models.ChatMode.ChatMode
import com.scalamc.models.entity.{BoundingBox, LivingEntity}
import com.scalamc.models.inventory.Inventory
import com.scalamc.packets.game.ClientSettingsPacket


case class Player(var name: String, entityId: Int, var uuid: UUID, session: Session, var location: Location, var settings: PlayerSettings = null) extends LivingEntity{
  override var previousLocation: Location = location
  override var inventory: Inventory = _

  override val typeId: Int = 0
  override val boundingBox: BoundingBox = BoundingBox(0.6, 1.95)
  override val nameId: String = "player"
}

case class PlayerSettings(var locale: String,
                          var viewDistance: Byte,
                          var chatMode: ChatMode,
                          var chatColors: Boolean,
                          var displayesSkinParts: Byte,
                          var mainHand: Byte){
  def this(packet: ClientSettingsPacket){
    this(packet.locale,
      packet.viewDistance,
      ChatMode(packet.chatMode.int),
      packet.chatColors,
      packet.displayedSkinParts,
      packet.mainHand.int.toByte)
  }
}

object ChatMode extends Enumeration{
  type ChatMode = Value
  val Full = Value(0)
  val System = Value(1)
  val None = Value(2)
}