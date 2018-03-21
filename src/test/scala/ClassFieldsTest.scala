import com.scalamc.packets.game.SpawnPositionPacket
import com.scalamc.utils.ClassFieldsCache
import org.scalatest.{FunSuite, GivenWhenThen, Matchers}

class ClassFieldsTest extends FunSuite with GivenWhenThen with Matchers {
  test("read") {

    println(ClassFieldsCache.getFields(SpawnPositionPacket()))
  }

}
