import org.scalatest.{FunSuite, GivenWhenThen, Matchers}

class MatchTest extends FunSuite with GivenWhenThen with Matchers {
  test("match"){
    var i = 0
    i match {
      case i if i==0 => println("0")
      case i => println("other")
    }
  }
}
