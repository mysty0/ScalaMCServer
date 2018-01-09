import com.scalamc.utils.VariableValueArray
import org.scalatest.{FunSuite, GivenWhenThen, Matchers}

class VariableValueArrayTest extends FunSuite with GivenWhenThen with Matchers  {

  test("write and read"){
    var arr = new VariableValueArray(13.toByte, 4)
    arr(0) = 10
    assert(arr(0)==10)
  }

  test("calculate need bits"){
    println(VariableValueArray.calculateNeededBits(1))
  }

}
