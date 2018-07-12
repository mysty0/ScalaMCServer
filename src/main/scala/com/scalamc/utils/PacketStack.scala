package com.scalamc.utils

import scala.collection.mutable

class PacketStack extends ByteStack {
  def this(bytes: Array[Byte]){
    this
    this ++= bytes.reverse
  }

  def handlePackets(parsePacketHandler: (ByteBuffer) => Unit) = {
    try {
      while (nonEmpty){
        parsePacketHandler(popPacketWith(popPacketLength()))
      }
    } catch {
      case e: Exception =>
        println("Packet parse error "+e)
    }
  }

  def popPacketLength(): Int = popVarInt()

  def popPacketWith(len: Int): ByteBuffer = popWith(len)
}

