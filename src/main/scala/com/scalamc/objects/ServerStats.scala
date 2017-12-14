package com.scalamc.objects

import io.circe.{Decoder, Encoder, HCursor}
import io.circe.generic.JsonCodec

@JsonCodec case class ServerVersion(name: String, protocol: Int)
@JsonCodec case class StatsPlayer(name: String, id: String)
@JsonCodec case class StatsPlayers(max:Int, online:Int, sample: Array[StatsPlayer])
@JsonCodec case class StatsDescription(text: String)

case class ServerStats(version: ServerVersion = ServerVersion("1.11.2", 316),
                       players: StatsPlayers = StatsPlayers(100, 0, new Array[StatsPlayer](0)),
                       description:StatsDescription = StatsDescription("Hello world")) {
  //var version = ServerVersion("1.11.2", 316)
  //var players = StatsPlayers(100, 0, new Array[StatsPlayer](0))
  //var description = StatsDescription("Hello world, I am going to be a best server in the world")
}
object ServerStats{
  val serverStats = ServerStats()
}

