package com.scalamc.models.world.chunk

import com.scalamc.utils.{ByteBuffer, NibbleArray, VariableValueArray}
import com.scalamc.utils.BytesUtils._

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
  val DEFAULT_BLOCK_LIGHT = 0
  val DEFAULT_SKYLIGHT = 0xF
  /**
    * The number of bits per block used in the global palette.
    */
  val GLOBAL_PALETTE_BITS_PER_BLOCK = 13
}

class ChunkSection(){

  var count = 0
  var palette = Array[Int]()
  var data:VariableValueArray = _
  private var skyLight = new NibbleArray(ChunkSection.ARRAY_SIZE, ChunkSection.DEFAULT_SKYLIGHT.toByte)
  private var blockLight = new NibbleArray(ChunkSection.ARRAY_SIZE, ChunkSection.DEFAULT_SKYLIGHT.toByte)

  loadTypeArray(new Array[Int](ChunkSection.ARRAY_SIZE))


  def index(x: Int, y: Int, z: Int): Int = {
    if (x < 0 || z < 0 || x >= Chunk.WIDTH || z >= Chunk.HEIGHT) throw new IndexOutOfBoundsException("Coords (x=" + x + ",z=" + z + ") out of section bounds")
    (y & 0xf) << 8 | z << 4 | x
  }

  def loadTypeArray(types: Array[Int]): Unit = {
    if (types.length != ChunkSection.ARRAY_SIZE) throw new IllegalArgumentException("Types array length was not " + ChunkSection.ARRAY_SIZE + ": " + types.length)
    // Build the palette, and the count
    count = 0
    for (t <- types) {
      if (t != 0) count += 1
      if (!palette.contains(t)) palette +:= t
    }
    // Now that we've built a palette, build the list
    var bitsPerBlock = VariableValueArray.calculateNeededBits(palette.length)
    if (bitsPerBlock < 4) bitsPerBlock = 4
    else if (bitsPerBlock > 8) {
      palette = null
      bitsPerBlock = ChunkSection.GLOBAL_PALETTE_BITS_PER_BLOCK
    }
    this.data = new VariableValueArray(bitsPerBlock, ChunkSection.ARRAY_SIZE)
    for(i <- 0 until ChunkSection.ARRAY_SIZE){
      if (palette != null) data(i) = palette.indexOf(types(i))
      else data(i) = types(i)
    }
  }

  /**
    * Optimizes this chunk section, removing unneeded palette entries and
    * recounting non-air blocks. This is an expensive operation, but
    * occasionally performing it will improve sending the section.
    */
  def optimize(): Unit = {
    loadTypeArray(getTypes)
  }

  /**
    * Recount the amount of non-air blocks in the chunk section.
    */
  def recount(): Unit = {
    count = 0
    for(i <- 0 until ChunkSection.ARRAY_SIZE){
      var `type` = data(i)
      if (palette != null) `type` = palette(`type`)
      if (`type` != 0) count += 1
    }
  }



  /**
    * Gets the type at the given coordinates.
    *
    * @param x The x coordinate, for east and west.
    * @param y The y coordinate, for up and down.
    * @param z The z coordinate, for north and south.
    * @return A type ID
    */
  def getType(x: Int, y: Int, z: Int): Int = {
    var value = data(index(x, y, z))
    if (palette != null) value = palette(value)
    value.toChar
  }

  /**
    * Sets the type at the given coordinates.
    *
    * @param x     The x coordinate, for east and west.
    * @param y     The y coordinate, for up and down.
    * @param z     The z coordinate, for north and south.
    * @param value The new type ID for that coordinate.
    */
  def setType(x: Int, y: Int, z: Int, value: Int): Unit = {
    val oldType = getType(x, y, z)
    if (oldType != 0) count -= 1
    if (value != 0) count += 1
    var encoded = 0
    if (palette != null) {
      encoded = palette.indexOf(value)
      if (encoded == -1) {
        encoded = palette.length
        palette +:= value
        if (encoded > data.valueMask) { // This is the situation where it can become expensive:
          // resize the array
          if (data.bitsPerValue == 8) {
            data = data.increaseBitsPerValueTo(ChunkSection.GLOBAL_PALETTE_BITS_PER_BLOCK)
            // No longer using the global palette; need to manually
            // recalculate
            var i = 0
            for(i <- 0 until ChunkSection.ARRAY_SIZE){
              val oldValue = data(i)
              val newValue = palette(oldValue)
              data(i) = newValue
            }
            palette = null
            encoded = value
          }
          else { // Using the global palette: automatically resize
            data = data.increaseBitsPerValueTo(data.bitsPerValue + 1)
          }
        }
      }
    }
    else encoded = value
    data(index(x, y, z)) = encoded
  }

  /**
    * Returns the block type array. Do not modify this array.
    *
    * @return The block type array.
    */
  def getTypes: Array[Int] = {
    var types = new Array[Int](ChunkSection.ARRAY_SIZE)
    var i = 0
    for(i <- 0 until ChunkSection.ARRAY_SIZE) {
      var t = data(i)
      if (palette != null) t = palette(t)
      types(i) = t
    }
    types
  }

  @throws[IllegalStateException]
  def writeToBuf(buf: ByteBuffer, skylight: Boolean): Unit = {
    if (isEmpty) throw new IllegalStateException("Can't write empty sections")
    buf += data.bitsPerValue // Bit per value -> varies

    if (palette == null) buf.writeVarInt(0) // Palette size -> 0 -> Use the global palette
    else {
      buf.writeVarInt(palette.length) // Palette size
      // Foreach loops can't be used due to autoboxing
      for (i <- palette) buf.writeVarInt(i)
    }
    val backing = data.backing
    buf.writeVarInt(backing.length)
    //buf.ens((backing.length << 3) + blockLight.byteSize + (if (skylight) skyLight.byteSize
    //else 0))
    for (value <- backing) {
      buf += value
    }
    buf += blockLight.getRawData
    if (skylight) buf += skyLight.getRawData
  }

  def isEmpty: Boolean = count == 0
}