package com.scalamc.packets.game.player.inventory

import com.xorinc.scalanbt.tags._

class SlotRaw(var itemId: Short = 0,
              var itemCount: Option[Byte] = Some(0.toByte),
              var itemDamage: Option[Short] = Some(0),
              var nbt: Option[TagCompound] = Some(new TagCompound(Seq(("", TagInt(0)))))) {
  def this(){this(0)}
}
