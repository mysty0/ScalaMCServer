package com.scalamc.packets.game.player

import com.scalamc.models.Position
import com.scalamc.models.enums.PacketEnum.EnumVal
import com.scalamc.models.utils.VarInt
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo}
import com.scalamc.packets.game.player.BlockFace.BlockFaceVal
import com.scalamc.packets.game.player.DiggingStatus.DiggingStatusVal
import com.scalamc.utils.ByteStack

case class PlayerDiggingPacket(var status: DiggingStatusVal = DiggingStatus.StartedDigging,
                               var position: Position = Position(),
                               var face: BlockFaceVal = BlockFace.Bottom)
  extends Packet(PacketInfo(Map(340 -> 0x14.toByte, 335 -> 0x14.toByte), direction = PacketDirection.Server)){
  def this(){this(DiggingStatus.StartedDigging)}
}

object DiggingStatus extends Enumeration {
  case class DiggingStatusVal(override var value: Any = 0) extends EnumVal
  //type DiggingStatusVal = Value
  val StartedDigging           = DiggingStatusVal(VarInt(0))
  val CancelledDigging         = DiggingStatusVal(VarInt(1))
  val FinishedDigging          = DiggingStatusVal(VarInt(2))
  val DropItemStack            = DiggingStatusVal(VarInt(3))
  val DropItem                 = DiggingStatusVal(VarInt(4))
  val ShootArrowOrFinishEating = DiggingStatusVal(VarInt(5))
  val SwapItemInHand           = DiggingStatusVal(VarInt(6))
}

object BlockFace extends Enumeration {
  case class BlockFaceVal(override var value: Any = 0) extends EnumVal

  val Bottom  = BlockFaceVal(0.toByte)//-Y
  val Top     = BlockFaceVal(1.toByte)//+Y
  val North   = BlockFaceVal(2.toByte)//-Z
  val South   = BlockFaceVal(3.toByte)//+Z
  val West    = BlockFaceVal(4.toByte)//-X
  val East    = BlockFaceVal(5.toByte)//+X
}
