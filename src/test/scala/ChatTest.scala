import com.scalamc.models.Chat
import io.circe.Printer
import org.scalatest.{FunSuite, GivenWhenThen, Matchers}
import io.circe.generic.auto._
import io.circe.syntax._

class ChatTest extends FunSuite with GivenWhenThen with Matchers {
  test("encode"){
    val pr = Printer.noSpaces.copy(dropNullKeys = true)
    println(pr.pretty(Chat(" ").asJson))
  }
}
