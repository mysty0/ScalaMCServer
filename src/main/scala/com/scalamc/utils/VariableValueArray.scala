package com.scalamc.utils

object VariableValueArray {
  /**
    * Calculates the number of bits that would be needed to store the given
    * value.
    *
    * @param  number the value
    * @return The number of bits that would be needed to store the value.
    */
  def calculateNeededBits(number: Int): Int = {
    var num = 0
    var count = 0
    do {
      count += 1
      num >>>= 1
    } while (num!=0)
    count
  }
}

final class VariableValueArray(val bitsPerValue: Int, val capacity: Int) extends Cloneable {
  //if (capacity < 0) throw new IllegalArgumentException(String.format("capacity (%s) must not be negative", capacity))
  //if (bitsPerValue < 1) throw new IllegalArgumentException(String.format("bitsPerValue (%s) must not be less than 1", bitsPerValue))
  //if (bitsPerValue > 64) throw new IllegalArgumentException(String.format("bitsPerValue (%s) must not be greater than 64", bitsPerValue))

  var backing = new Array[Long](Math.ceil((bitsPerValue * capacity) / 64.0).toInt)
  var valueMask = (1L << bitsPerValue) - 1L


  def apply(index: Int): Int = {
    var ind = index
    checkIndex(index)
    if(bitsPerValue==0) return 0
    ind *= bitsPerValue
    var i0 = ind >> 6
    val i1 = ind & 0x3f
    var value = backing(i0) >>> i1
    val i2 = i1 + bitsPerValue
    // The value is divided over two long values
    if (i2 > 64) value |= backing({
      i0 += 1; i0
    }) << 64 - i1
    (value & valueMask).toInt
  }

  def update(index: Int, value: Int): Unit = {
    var ind = index
    checkIndex(ind)
    //if (value < 0) throw new IllegalArgumentException(String.format("value (%s) must not be negative", value))
    //if (value > valueMask) throw new IllegalArgumentException(String.format("value (%s) must not be greater than %s", value, valueMask))
    ind *= bitsPerValue
    var i0 = ind >> 6
    val i1 = ind & 0x3f
    backing(i0) = this.backing(i0) & ~(this.valueMask << i1) | (value & valueMask) << i1
    val i2 = i1 + bitsPerValue
    if (i2 > 64) {
      i0 += 1
      backing(i0) = backing(i0) & ~((1L << i2 - 64) - 1L) | value >> 64 - i1
    }
  }

  private def checkIndex(index: Int): Unit = {
   // if (index < 0) throw new IndexOutOfBoundsException(String.format("index (%s) must not be negative", index))
    //if (index >= capacity) throw new IndexOutOfBoundsException(String.format("index (%s) must not be greater than the capacity (%s)", index, capacity))
  }

  /**
    * Creates a new VariableValueArray with the contents of this one, and the
    * given bits per value.
    *
    * @param newBitsPerValue
    * The new value. Must be larger than the current value (
    * { @link #getBitsPerValue()}).
    * @throws IllegalArgumentException
    * If newBitsPerValue is less than or equal to the current bits
    * per value. Setting it to the same size would be a waste of
    * resources, and decreasing could lead to data loss.
    * @return A new VariableValueArray
    */
  def increaseBitsPerValueTo(newBitsPerValue: Int): VariableValueArray = {
    if (newBitsPerValue < this.bitsPerValue) throw new IllegalArgumentException("Cannot decrease bits per value!  (was " + this.bitsPerValue + ", new size " + newBitsPerValue + ")")
    else if (newBitsPerValue == this.bitsPerValue) throw new IllegalArgumentException("Cannot resize to the same size!  (size was " + newBitsPerValue + ")")
    val returned = new VariableValueArray(newBitsPerValue, this.capacity)
    for (i <- 0 until this.capacity) {
      returned(i) = this(i)
    }
    returned
  }

  override def clone: VariableValueArray = {
    val clone = new VariableValueArray(this.bitsPerValue, this.capacity)
    System.arraycopy(this.backing, 0, clone.backing, 0, this.backing.length)
    clone
  }
}

