import akka.util.ByteString
import com.scalamc.actors.ConnectionHandler
import com.scalamc.models.enums.Difficulty.{Difficulty, Easy}
import com.scalamc.models.enums.Dimension.{Dimension, Overworld}
import com.scalamc.models.enums.GameMode.{Creative, GameMode}
import com.scalamc.models.enums.LevelType.{Default, LevelType}
import com.scalamc.models.utils.VarInt
import com.scalamc.objects.ServerStats
import com.scalamc.packets.{Packet, PacketInfo}
import com.scalamc.packets.login.{JoinGamePacket, LoginStartPacket, LoginSuccessPacket}
import org.scalatest.{FeatureSpec, FunSuite, GivenWhenThen, Matchers}
import com.scalamc.utils.ByteBuffer
import com.scalamc.utils.BytesUtils._
import org.clapper.classutil.ClassInfo
import com.scalamc.models.utils.VarInt._
import com.scalamc.packets.game.entity.{DestroyEntitiesPacket, EntityHeadRotationPacket}
import com.scalamc.packets.game.player.SpawnPlayerPacket

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
    //println(s"name ${Packet.packets.last._2.newInstance()}")
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




  test("WritePacketTest"){


    // Given
    var id: Int = 0
    var gamemode: GameMode = Creative
    var dimension: Dimension = Overworld
    var difficulty: Difficulty = Easy
    var maxPlayer: Byte = ServerStats.serverStats.players.max.toByte
    var levelType: LevelType = Default
    var reducedDebugInfo: Boolean = true
    implicit val protocolId = 340

    val packet = JoinGamePacket(id, gamemode, dimension, difficulty, maxPlayer, levelType, reducedDebugInfo).write()
    Given(javax.xml.bind.DatatypeConverter.printHexBinary(packet.toArray))
    //var comp = new ByteBuffer()+Array(0x23.toByte)+id+gamemode.toBytes+dimension.toBytes+difficulty.toBytes+Array(maxPlayer)+levelType.toBytes+Array(1)
    var lenBuff = new ByteBuffer()
    //lenBuff.writeVarInt(comp.length)
    //comp = lenBuff + comp.toArray
    //println(comp)
    //assert(packet == comp)
  }

  test("WriteRPacket"){

    implicit val protocolId = 340
    println(javax.xml.bind.DatatypeConverter.printHexBinary(EntityHeadRotationPacket(10, 50).write().toArray))
  }

  test("WriteDestroyEntitiesPacket"){
    implicit val protocolId = 340
    var ids = new ArrayBuffer[VarInt]()
    ids += VarInt(10)
    println(javax.xml.bind.DatatypeConverter.printHexBinary(DestroyEntitiesPacket(ids).write().toArray))
  }

}