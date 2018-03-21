import akka.util.ByteString
import com.scalamc.actors.ConnectionHandler
import com.scalamc.models.enums.Difficulty.{DifficultyVal, Easy}
import com.scalamc.models.enums.Dimension.{DimensionVal, Overworld}
import com.scalamc.models.enums.GameMode.{Creative, GameModeVal}
import com.scalamc.models.enums.LevelType.{Default, LevelTypeVal}
import com.scalamc.objects.ServerStats
import com.scalamc.packets.game.playerslist.{AddPlayerAction, PlayerListPacket}
import com.scalamc.packets.{Packet, PacketInfo}
import com.scalamc.packets.login.{JoinGamePacket, LoginStartPacket, LoginSuccessPacket}
import org.scalatest.{FeatureSpec, FunSuite, GivenWhenThen, Matchers}
import com.scalamc.utils.{ByteBuffer, PacketStack}
import com.scalamc.utils.BytesUtils._
import org.clapper.classutil.ClassInfo
import com.scalamc.models.utils.VarInt._

import scala.collection.mutable.ArrayBuffer

class PacketTest extends FunSuite with GivenWhenThen with Matchers {
//  test("reading_packet") {
//    Given("Buffer and packet")
//    val buffer = new ByteBuffer()
//    val packet = new LoginStartPacket()
//
//    When("read the packet")
//    packet.read(buffer)
//    println(s"name ${packet.name}")
//    Then("the read complite")
//    assert(packet.name != "")
//  }
//
  test("get_packet") {
    Given("packet")
    Packet
    When("get")
    //packet.read(buffer)
    println(s"name ${Packet.packets.last._2.newInstance()}")
    Then("the get complite")
    assert(Packet.packets.nonEmpty)
  }
//
//  test("write_packet"){
//    Given("packet")
//    var packet = new LoginSuccess("123", "123")
//    When("write")
//    var buff = packet.write()
//    Then("buff")
//  }

  test("ReadPacketLengthTest") {
    // Given
    val data = ByteString(9, 1, 1, 2, 3, 4, 5, 6, 7, 8)
    var pingPacket: ByteBuffer = null
    val stackPacket = new PacketStack(data.toArray)

    // When
    val len = stackPacket.popPacketLength()

    // Then
    assert(len == 9)
  }

  test("ReadPacketLengthEdgeTest") {
    // Given
    val data = ByteString(127, 1, 1, 2, 3, 4, 5, 6, 7, 8)
    var pingPacket: ByteBuffer = null
    val stackPacket = new PacketStack(data.toArray)

    // When
    val len = stackPacket.popPacketLength()

    // Then
    assert(len == 127)
  }

  test("ReadPacketLengthMultiByteTest") {
    // Given
    val data = ByteString(255, 255, 255, 255, 7 , 1, 1, 2, 3, 4, 5, 6, 7, 8)
    var pingPacket: ByteBuffer = null
    val stackPacket = new PacketStack(data.toArray)

    // When
    val len = stackPacket.popPacketLength()

    // Then
    assert(len == 2147483647)
  }


  test("ReadPingPacketTest") {
    val data = ByteString(9, 1, 1, 2, 3, 4, 5, 6, 7, 8)
    Given(data.toString())
    var pingPacket: ByteBuffer = null
    val stackPacket = new PacketStack(data.toArray)
    stackPacket.handlePackets((packet)=>{pingPacket = packet})
    assert(pingPacket == new ByteBuffer(Array(1, 1, 2, 3, 4, 5, 6, 7, 8)))
  }

  test("ReadPingAndLoginPacketTest") {
    val data = ByteString(9, 1, 1, 2, 3, 4, 5, 6, 7, 8, 6, 0, 1, 1, 1, 1, 1)
    Given(data.toString())
    var pingPacket: ByteBuffer = null
    val stackPacket = new PacketStack(data.toArray)
    stackPacket.handlePackets((packet)=>{pingPacket = packet})
    assert(pingPacket == new ByteBuffer(Array(0, 1, 1, 1, 1, 1)))
  }

  test("WritePacketTest"){


    // Given
    var id: Int = 0
    var gamemode: GameModeVal = Creative
    var dimension: DimensionVal = Overworld
    var difficulty: DifficultyVal = Easy
    var maxPlayer: Byte = ServerStats.serverStats.players.max.toByte
    var levelType: LevelTypeVal = Default
    var reducedDebugInfo: Boolean = true
    implicit val protocolId = 340

    val packet = JoinGamePacket(id, gamemode, dimension, difficulty, maxPlayer, levelType, reducedDebugInfo).write()
    Given(javax.xml.bind.DatatypeConverter.printHexBinary(packet.toArray))
    var comp = new ByteBuffer()+Array(0x23.toByte)+id+gamemode.toBytes+dimension.toBytes+difficulty.toBytes+Array(maxPlayer)+levelType.toBytes+Array(1)
    var lenBuff = new ByteBuffer()
    lenBuff.writeVarInt(comp.length)
    comp = lenBuff + comp.toArray
    println(comp)
    assert(packet == comp)
  }

  test("WritePacketWithBoolProperty"){

    implicit val protocolId = 340
    println(PlayerListPacket(1, 1, ArrayBuffer(AddPlayerAction())).write())
  }

}