package com.scalamc.models.metadata

import com.scalamc.models.utils.VarInt

class EntityMetadataRaw(var flags: Byte = 0x00.toByte,
                        var air: VarInt = VarInt(300),
                        var customName: String = "",
                        var isCustomNameVisible: Boolean = false,
                        var isSlient: Boolean = false,
                        var noGravity: Boolean = false) {

  def this(){this(0x00.toByte)}
}
