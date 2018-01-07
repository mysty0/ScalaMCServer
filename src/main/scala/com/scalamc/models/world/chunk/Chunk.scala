package com.scalamc.models.world.chunk

import java.util
import java.util.{HashSet, Set}

import com.scalamc.models.VarInt
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

  var sections = Array[ChunkSection]()
  var heightMap = Array[Byte]()
  var biomes = Array[Byte]()


  private def getSection(y: Int): ChunkSection = {
    val idx = y >> 4
    if (y < 0 || y >= Chunk.DEPTH || idx >= sections.length) return null
    sections(idx)
  }

  def initEmptyChunk(): Unit ={
    var secs = new Array[ChunkSection](Chunk.SEC_COUNT)
    for(i <- 0 until Chunk.SEC_COUNT)
      secs(i) = new ChunkSection()
    initializeSections(secs)
  }

  /**
    * Initialize this chunk from the given sections.
    *
    * @param initSections The { @link ChunkSection}s to use.  Should have a length of { @value #SEC_COUNT}.
    */
  def initializeSections(initSections: Array[ChunkSection]): Unit = {

    sections = new Array[ChunkSection](Chunk.SEC_COUNT)
    biomes = new Array[Byte](Chunk.WIDTH * Chunk.HEIGHT)
    heightMap = new Array[Byte](Chunk.WIDTH * Chunk.HEIGHT)
    var y = 0
    while (y < Chunk.SEC_COUNT && y < initSections.length) {
      if (initSections(y) != null) initializeSection(y, initSections(y))

      {
        y += 1; y - 1
      }
    }
  }

  private def initializeSection(y: Int, section: ChunkSection): Unit = {
    sections(y) = section
  }

  /**
    * Gets the type of a block within this chunk.
    *
    * @param x The X coordinate.
    * @param z The Z coordinate.
    * @param y The Y coordinate.
    * @return The type.
    */
  def getType(x: Int, z: Int, y: Int): Int = {
    val section = getSection(y)
    if (section == null) 0
    else section.getType(x, y, z) >> 4
  }


  /**
    * Sets the type of a block within this chunk.
    *
    * @param x    The X coordinate.
    * @param z    The Z coordinate.
    * @param y    The Y coordinate.
    * @param t The type.
    */
  def setType(x: Int, z: Int, y: Int, t: Int): Unit = {
    if (t < 0 || t > 0xfff) throw new IllegalArgumentException("Block type out of range: " + t)
    var section = getSection(y)
    if (section == null) {
      if (t == 0) { // don't need to create chunk for air
        return
      }
      else { // create new ChunkSection for this y coordinate
        val idx = y >> 4
        if (y < 0 || y >= Chunk.DEPTH || idx >= sections.length) { // y is out of range somehow
          return
        }
        section = new ChunkSection
        sections(idx) = section
      }
    }
    // update the air count and height map
    val heightIndex = z * Chunk.WIDTH + x
    if (t == 0) if (heightMap(heightIndex) == y + 1) { // erased just below old height map -> lower
      heightMap(heightIndex) = lowerHeightMap(x, y, z).toByte
    }
    else if (heightMap(heightIndex) <= y) { // placed between old height map and top -> raise
      heightMap(heightIndex) = Math.min(y + 1, 255).toByte
    }
    // update the type - also sets metadata to 0
    section.setType(x, y, z, t << 4)
    if (section.isEmpty) { // destroy the empty section
      sections(y / Chunk.SEC_DEPTH) = null
      return
    }
  }

  /**
    * Scan downwards to determine the new height map value.
    */
  def lowerHeightMap(x: Int, y: Int, z: Int): Int = {
    for (ny <- (0 until y).reverse) {
      if (getType(x, z, ny) != 0) {
        ny + 1
      }
    }
    y + 1
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
          sections(i).writeToBuf(buf, skylight)
        }
      }
    }
    if (entireChunk && biomes != null) {
      buf += biomes
    }
    ChunkPacket(x, y, entireChunk, VarInt(sectionBitmask), buf)
  }

}
