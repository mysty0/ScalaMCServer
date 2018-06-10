import com.scalamc.models.utils.VarInt
import com.scalamc.packets.game.player.{AddPlayerListAction, PlayerItem, PlayerListItemPacket}
import com.scalamc.utils.BytesUtils._
import com.scalamc.packets.Packet._
import org.scalatest.{FunSuite, GivenWhenThen, Matchers}

import scala.collection.mutable.ArrayBuffer

class PlayerListItemPacketTest extends FunSuite with GivenWhenThen with Matchers {
  test("write"){
    var items = new ArrayBuffer[PlayerItem]()
    items += PlayerItem()
    implicit var protocol = 340
    var packet = PlayerListItemPacket(VarInt(0), items).write()
    println(javax.xml.bind.DatatypeConverter.printHexBinary(packet.toArray))
  }
}
