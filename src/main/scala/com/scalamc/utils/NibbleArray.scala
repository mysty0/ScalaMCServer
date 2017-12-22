package com.scalamc.utils

import java.util
import java.util.Arrays

import com.google.common.base.Preconditions.checkArgument

class NibbleArray {
  private var data: Array[Byte] = _

  /**
    * Construct a new NibbleArray with the given size in nibble, filled with the specified nibble value.
    *
    * @param size  The number of nibbles in the array.
    * @param value The value to fill the array with.
    * @throws IllegalArgumentException If size is not positive and even.
    */
  def this(size: Int, value: Byte) {
    this()
    //checkArgument(size > 0 && size % 2 == 0, "size must be positive even number, not " + size)
    data = new Array[Byte](size / 2)
    if (value != 0) fill(value)
  }

  /**
    * Construct a new NibbleArray with the given size in nibbles.
    *
    * @param size The number of nibbles in the array.
    * @throws IllegalArgumentException If size is not positive and even.
    */
  def this(size: Int) {
    this(size, 0.toByte)
  }

  /**
    * Construct a new NibbleArray using the given underlying bytes. No copy is created.
    *
    * @param data The raw data to use.
    */
  def this(data: Byte*) {
    this()
    this.data = data.toArray
  }

  /**
    * Get the size in nibbles.
    *
    * @return The size in nibbles.
    */
  def size: Int = 2 * data.length

  /**
    * Get the size in bytes, one-half the size in nibbles.
    *
    * @return The size in bytes.
    */
  def byteSize: Int = data.length

  /**
    * Get the nibble at the given index.
    *
    * @param index The nibble index.
    * @return The value of the nibble at that index.
    */
  def apply(index: Int): Byte = {
    val `val`: Byte = data(index / 2)
    if (index % 2 == 0) (`val` & 0x0f).toByte
    else ((`val` & 0xf0) >> 4).toByte
  }

  /**
    * Set the nibble at the given index to the given value.
    *
    * @param index The nibble index.
    * @param value The new value to store.
    */
  def update(index: Int, value: Byte): Unit = {
    var valu:Byte = value
    valu = (valu & 0xf0.toByte).toByte
    val half: Int = index / 2
    val previous: Byte = data(half)
    if (index % 2 == 0) data(half) = (previous & 0xf0 | valu).toByte
    else data(half) = (previous & 0x0f | valu << 4).toByte
  }

  /**
    * Fill the nibble array with the specified value.
    *
    * @param value The value nibble to fill with.
    */
  def fill(value: Byte): Unit = {
    var v:Byte = value
    v = (v & 0xf.toByte).toByte
    util.Arrays.fill(data, (v << 4 | v).toByte)
  }

  /**
    * Get the raw bytes of this nibble array. Modifying the returned array will modify the internal representation of this nibble array.
    *
    * @return The raw bytes.
    */
  def getRawData: Array[Byte] = data

  /**
    * Copies into the raw bytes of this nibble array from the given source.
    *
    * @param source The array to copy from.
    * @throws IllegalArgumentException If source is not the correct length.
    */
  def setRawData(source: Byte*): Unit = {
    //checkArgument(source.length == data.length, "expected byte array of length " + data.length + ", not " + source.length)
    System.arraycopy(source, 0, data, 0, source.length)
  }
}

