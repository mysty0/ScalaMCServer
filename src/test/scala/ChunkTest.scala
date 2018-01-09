import com.scalamc.models.world.Block
import com.scalamc.models.world.chunk.Chunk
import org.scalatest.{FunSuite, GivenWhenThen, Matchers}

class ChunkTest extends FunSuite with GivenWhenThen with Matchers {
  test("set and get type"){
    var chunk = new Chunk(0,0)
    chunk.setBlock(0,0,0, Block(10, 0))
    println(chunk.getSection(0).getBlock(0,0,0))
    assert(chunk.getBlock(0,0,0).id == 10)
  }
}
