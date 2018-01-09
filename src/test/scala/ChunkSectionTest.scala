import com.scalamc.models.world.Block
import com.scalamc.models.world.chunk.ChunkSection
import org.scalatest.{FunSuite, GivenWhenThen, Matchers}

class ChunkSectionTest extends FunSuite with GivenWhenThen with Matchers {
  test("write and read type"){
    var chunkSection = new ChunkSection()
    chunkSection.setBlock(0, 0, 0, Block(4, 0))
    assert(chunkSection.getBlock(0,0,0).id == 4 && chunkSection.getBlock(0,1,0).id==0)
  }

}
