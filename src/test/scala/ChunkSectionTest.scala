import akka.util.ByteString
import com.scalamc.models.world.Block
import com.scalamc.models.world.chunk.ChunkSection
import com.scalamc.utils.{ByteBuffer, ByteStack}
import org.scalatest.{FunSuite, GivenWhenThen, Matchers}

class ChunkSectionTest extends FunSuite with GivenWhenThen with Matchers {
  test("write and read type"){
    var chunkSection = new ChunkSection()
    chunkSection.setBlock(0, 0, 0, Block(4, 0))
    assert(chunkSection.getBlock(0,0,0).id == 4 && chunkSection.getBlock(0,1,0).id==0)
  }

  test("write and read type1"){
    val bitsPerBlock = 13
    val blockId = 4
    val lastBlockId = 7
    val metadata: Byte = 0

    var chunkSection = new ChunkSection()
    chunkSection.setBlock(0, 0, 0, Block(blockId, metadata))
    chunkSection.setBlock(15, 15, 15, Block(lastBlockId, metadata))
    var buf: ByteBuffer = new ByteBuffer()
    chunkSection.writeToBuff(buf, true)
    var stack = new ByteStack(buf.toArray)
    assert(stack.pop() == bitsPerBlock)
    assert(stack.pop() == 0)
    assert(stack.popVarInt() == 4096 * bitsPerBlock / 64)

    assert(stack.popLong() == ((blockId << 4) | metadata))
    assert(stack.popLong() == 0)
    stack.popWith((4096 * bitsPerBlock / 64 - 3) * 8)
    val lastBlock = stack.popLong()
    println(lastBlock)
    assert((lastBlock >> (64 - 13)) == ((lastBlockId << 4) | metadata))

    assert(stack.length == 4096)
    assert(stack.pop() == 0xFF.toByte)
    stack.popWith(4096 - 2)
    assert(stack.pop() == 0xFF.toByte)
  }

}
