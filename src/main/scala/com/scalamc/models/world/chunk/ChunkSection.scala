package com.scalamc.models.world.chunk

import com.scalamc.models.world.Block
import com.scalamc.utils.{ByteBuffer, NibbleArray, VariableValueArray}
import com.scalamc.utils.BytesUtils._

import scala.collection.mutable.ListBuffer

object ChunkSection {
  /**
    * The number of blocks in a chunk section, and thus the number of elements
    * in all arrays used for it.
    */
  val ARRAY_SIZE: Int = Chunk.WIDTH * Chunk.HEIGHT * Chunk.SEC_DEPTH
  /**
    * Block and sky light levels to use for empty chunk sections.
    */
  val EMPTY_BLOCK_LIGHT = 0
  val EMPTY_SKYLIGHT = 0
  /**
    * The default values for block and sky light, used on new chunk sections.
    */
  val DEFAULT_BLOCK_LIGHT: Byte = 0xF.toByte
  val DEFAULT_SKYLIGHT: Byte = 0xF.toByte
  /**
    * The number of bits per block used in the global palette.
    */
  val GLOBAL_PALETTE_BITS_PER_BLOCK: Byte = 13

  val MIN_PALETTE_BITS_PER_BLOCK: Byte = 4
}

class ChunkSection(){
  var blocks: VariableValueArray = new VariableValueArray(ChunkSection.MIN_PALETTE_BITS_PER_BLOCK, ChunkSection.ARRAY_SIZE)

  var palette: ListBuffer[Int] = ListBuffer[Int](0)

  var skyLight = new NibbleArray(ChunkSection.ARRAY_SIZE, ChunkSection.DEFAULT_BLOCK_LIGHT)
  var blockLight = new NibbleArray(ChunkSection.ARRAY_SIZE, ChunkSection.DEFAULT_BLOCK_LIGHT)

  var count = 0

  def this(palette: ListBuffer[Int] = ListBuffer[Int](0)){
    this()
    this.palette = palette
    if(palette != null)
    blocks = blocks.increaseBitsPerValueTo(palette.size)
    else blocks = new VariableValueArray(ChunkSection.GLOBAL_PALETTE_BITS_PER_BLOCK, ChunkSection.ARRAY_SIZE)
  }

  def isEmpty = {count == 0}

  def index(x: Int, y: Int, z: Int): Int = (y & 0xf) << 8 | z << 4 | x

  def setBlock(x: Int, y: Int, z: Int, block: Block): Unit ={
    val oldType = getBlock(x, y, z)
    if (oldType.id != 0) count -= 1
    if (block.id!= 0) count += 1

    var t = block.id << 4 | block.metadata

    var encoded = t
    if (palette != null) {
      encoded = palette.indexOf(t)
      if (encoded == -1) {
        encoded = palette.length
        palette += t
        if (encoded > blocks.valueMask) { // This is the situation where it can become expensive:
          // resize the array
          if (blocks.bitsPerValue == 8) {
            blocks = blocks.increaseBitsPerValueTo(ChunkSection.GLOBAL_PALETTE_BITS_PER_BLOCK)
            // No longer using the global palette; need to manually
            // recalculate
            for (i <- 0 until ChunkSection.ARRAY_SIZE) {
              val oldValue = blocks(i)
              val newValue = palette(oldValue)
              blocks(i) = newValue
              palette = null
            }
          } else { // Using the global palette: automatically resize
            blocks = blocks.increaseBitsPerValueTo(blocks.bitsPerValue + 1)
          }
        }
      }
    }
    println("set block id", t, "pall id", encoded)
    blocks(index(x, y, z)) = encoded
    blocks.backing.foreach(l => print(l.toHexString+" "))
    println()
    println("bpv", blocks.bitsPerValue)
  }

  def getBlock(x: Int, y: Int, z: Int) = {
    val data = if(palette==null) blocks(index(x, y, z)) else palette(blocks(index(x, y, z)))
    Block(data >> 4, (data & 0xF).toByte)
  }


  def writeToBuff(buff: ByteBuffer, skyLight: Boolean): Unit ={
    buff += blocks.bitsPerValue.toByte
    println(javax.xml.bind.DatatypeConverter.printHexBinary(buff.toArray))
    if(palette != null && palette.nonEmpty) {
      buff.writeVarInt(palette.size)
      //palette.foreach(t => buff.writeVarInt(t))
      val itr = palette.iterator
      while(itr.hasNext) buff.writeVarInt(itr.next())
    } else buff.writeVarInt(0)
    //buff.writeVarInt(0)
    println(javax.xml.bind.DatatypeConverter.printHexBinary(buff.toArray))
    buff.writeVarInt(blocks.backing.length)
    blocks.backing.foreach(l => buff += l)

    buff += blockLight.getRawData
    if(skyLight) buff += this.skyLight.getRawData
    println(javax.xml.bind.DatatypeConverter.printHexBinary(buff.toArray))
  }

}