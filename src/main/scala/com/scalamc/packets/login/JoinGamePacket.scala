package com.scalamc.packets.login

import com.scalamc.models.enums.GameMode._
import com.scalamc.models.enums.Difficulty._
import com.scalamc.models.enums.Dimension._
import com.scalamc.models.enums.LevelType._
import com.scalamc.objects.ServerStats
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo, PacketState}

case class JoinGamePacket(var id: Int = 0,
                     var gamemode: GameModeVal = Creative,
                     var dimension: DimensionVal = Overworld,
                     var difficulty: DifficultyVal = Easy,
                     var maxPlayer: Byte = ServerStats.serverStats.players.max.toByte,
                     var levelType: LevelTypeVal = Default,
                     var reducedDebugInfo: Boolean = true) extends Packet(PacketInfo(0x23.toByte, PacketState.Login, PacketDirection.Client)){

  def this(){this(0)}

}
