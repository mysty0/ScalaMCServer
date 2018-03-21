package com.scalamc.models.world.chunk

import java.util
import java.util.{HashSet, Set}

import com.scalamc.models.utils.VarInt
import com.scalamc.models.world.Block
import com.scalamc.packets.game.ChunkPacket
import com.scalamc.utils.ByteBuffer

object Chunk{
  val WIDTH = 16
  val HEIGHT = 16
  val DEPTH = 256
  val SEC_DEPTH = 16
  val SEC_COUNT = DEPTH / SEC_DEPTH
}

class Chunk(x: Int, y: Int) {

  var sections = new Array[ChunkSection](16)
  var heightMap = Array[Byte]()
  var biomes = new Array[Byte](256)

  def getSection(y: Int): ChunkSection ={
    sections(y/Chunk.SEC_DEPTH)
  }

  def setBlock(x: Int, y: Int, z: Int, block: Block): Unit ={
    var sec = getSection(y)
    if(sec == null){
      if(block.id==0) return
      sec = new ChunkSection()
      sections(y/Chunk.SEC_DEPTH) = sec
    }

    sec.setBlock(x, y, z, block)
  }

  def getBlock(x: Int, y: Int, z: Int): Block ={
    val section = getSection(y)
    if (section == null) Block(0,0)
    else section.getBlock(x, y, z)
  }

  def toPacket(skylight: Boolean, entireChunk: Boolean): ChunkPacket = {
    var sectionBitmask: Int = 0

    // filter sectionBitmask based on actual chunk contents
    if (sections != null) {
      val maxBitmask: Int = (1 << sections.length) - 1
      if (entireChunk) sectionBitmask = maxBitmask
      else sectionBitmask &= maxBitmask
      for(i <- sections.indices){
        if (sections(i) == null || sections(i).isEmpty) { // remove empty sections from bitmask
          sectionBitmask &= ~(1 << i)
        }
      }
    }

    val buf = new ByteBuffer()

    if (sections != null) { // get the list of sections
      for(i <- sections.indices){
        if ((sectionBitmask & 1 << i) != 0) {
          sections(i).writeToBuff(buf, skylight)
        }
      }
    }
    if (entireChunk && biomes != null) {
      buf += biomes
    }
    ChunkPacket(x, y, entireChunk, VarInt(sectionBitmask), buf)
  }

}
