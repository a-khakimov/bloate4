import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class TestTest extends AnyFlatSpec with should.Matchers {

  it should "be success" in {
    1 + 1 shouldBe 2
  }
}
