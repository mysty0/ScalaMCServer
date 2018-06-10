package com.scalamc.models.metadata

case class EntityMetadata(var onFire: Boolean = false,
                     var crouched: Boolean = false,
                     var unused: Boolean = false,
                     var sprinting: Boolean = false,
                     var invisible: Boolean = false,
                     var glowingEffect: Boolean = false,
                     var flyingWithElytra: Boolean = false,
                     var air: Int = 300,
                     var customName: String = "",
                     var isCustomNameVisible: Boolean = false,
                     var isSlient: Boolean = false,
                     var noGravity: Boolean = false) {

  def toRawMetadata: EntityMetadataRaw = {
    var bitMask:Int = 0x00.toByte
    if(onFire)            bitMask &= 0x02
    if(crouched)          bitMask &= 0x04
    if(unused)            bitMask &= 0x08
    if(sprinting)         bitMask &= 0x10
    if(invisible)         bitMask &= 0x20
    if(glowingEffect)     bitMask &= 0x40
    if(flyingWithElytra)  bitMask &= 0x80

    new EntityMetadataRaw(bitMask.toByte, air, customName, isCustomNameVisible, isSlient, noGravity)
  }

}
