package com.scalamc.objects

import com.scalamc.models.Chat
import io.circe.{Decoder, Encoder, HCursor}
import io.circe.generic.JsonCodec

@JsonCodec case class ServerVersion(name: String, protocol: Int)
@JsonCodec case class StatsPlayer(name: String, id: String)
@JsonCodec case class StatsPlayers(max:Int, online:Int, sample: Array[StatsPlayer])

case class ServerStats(version: ServerVersion = ServerVersion("1.12.2", 340),
                       players: StatsPlayers = StatsPlayers(100, 0, new Array[StatsPlayer](0)),
                       description:Chat = Chat("Hello world")) {
}
object ServerStats{
  val serverStats = ServerStats()
  def getStatus(implicit protocolId: Int) = getStatusWithProtocolId(protocolId)
  def getStatusWithProtocolId(protocol: Int) = ServerStats(ServerVersion("1.12.2", protocol))
}

