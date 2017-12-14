import com.scalamc.packets.{Packet, PacketInfo}
import com.scalamc.packets.login.LoginStartPacket
import com.scalamc.packets.login.LoginSuccess
import org.scalatest.{FeatureSpec, FunSuite, GivenWhenThen, Matchers}
import com.scalamc.utils.ByteBuffer
import org.clapper.classutil.ClassInfo

class PacketTest extends FunSuite with GivenWhenThen with Matchers {
  test("reading_packet") {
    Given("Buffer and packet")
    val buffer = new ByteBuffer()
    val packet = new LoginStartPacket()

    When("read the packet")
    packet.read(buffer)
    println(s"name ${packet.name}")
    Then("the read complite")
    assert(packet.name != "")
  }

  test("get_packet") {
    Given("packet")
    Packet
    When("get")
    //packet.read(buffer)
    println(s"name ${Packet.packets.last._2.newInstance()}")
    Then("the get complite")
    assert(Packet.packets.nonEmpty)
  }

  test("write_packet"){
    Given("packet")
    var packet = new LoginSuccess("123", "123")
    When("write")
    var buff = packet.write()
    Then("buff")
  }

}