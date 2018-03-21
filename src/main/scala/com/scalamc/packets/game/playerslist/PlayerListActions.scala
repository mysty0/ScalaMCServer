package com.scalamc.packets.game.playerslist

import java.util.UUID

import com.scalamc.models.utils.VarInt
import com.scalamc.models.utils.VarInt._
import com.scalamc.packets.BoolProperty

abstract class PlayerListAction(var uUID: UUID = UUID.randomUUID())

class AddPlayerActionProperty(var name: String = "", var value: String = "", var signed: Boolean = false)

case class AddPlayerAction(var name: String = "",
                           var propertys: Array[AddPlayerActionProperty] = Array(),
                           var gameMode: VarInt = 0,
                           var ping: VarInt = 0,
                           @BoolProperty var displayName: String = "")
  extends PlayerListAction
