import akka.util.ByteString
import com.scalamc.models.world.Block
import com.scalamc.models.world.chunk.{Chunk, ChunkSection}
import org.scalatest.{FunSuite, GivenWhenThen, Matchers}
import com.scalamc.packets.Packet._
import com.scalamc.utils.{ByteBuffer, ByteStack}

class ChunkTest extends FunSuite with GivenWhenThen with Matchers {
  test("set and get type"){
    var chunk = new Chunk(0,0)
    chunk.setBlock(0,0,0, Block(10, 0))
    println(chunk.getSection(0).getBlock(0,0,0))
    assert(chunk.getBlock(0,0,0).id == 10)
  }

  test("test chunk packet"){
    // given
    val x = 10
    val z = 20
    val groundUpContinues = true
    val sectionNum: Byte = 0
    val blockId = 10
    implicit val protocolId = 340

    // when
    var chunk = new Chunk(x, z)
    chunk.setBlock(0, sectionNum * 16, 0, Block(blockId, 0))
    var buf: ByteString = chunk.toPacket(true, groundUpContinues)
    var stack = new ByteStack(buf.toArray)
    assert(stack.popVarInt() == stack.length)
    assert(stack.popVarInt() == 0x20)
    assert(stack.popInt() == x)
    assert(stack.popInt() == z)
    assert(stack.pop() == (if (groundUpContinues) 1 else 0))
    assert(stack.popVarInt() == (1 << sectionNum))
    assert(stack.popVarInt() == stack.length - 1)

    var chunkSection = new ChunkSection()
    chunkSection.setBlock(0, 0, 0, Block(blockId, 0))
    var bufSection: ByteBuffer = new ByteBuffer()
    chunkSection.writeToBuff(bufSection, true)
    assert(stack.popWith(stack.length - 1).toString() == bufSection.toString())
  }

}
