import org.scalatest.{FreeSpec, Matchers}
import org.scalatest.OptionValues._
import services.LadderService

/**
  * Created by me on 18/01/2017.
  */
class LadderParseTest extends FreeSpec with Matchers {
  "TimedUserMessage" - {
    "is extracted" in {
      val tme = LadderService
        .TimedUserMessageExtract(Map("egg" -> "egghead").get)
        .unapply(LadderParseTest.sampleMessage)
        .value
      assert(tme.gibbed)
      assert(!tme.scored)
      assert(!tme.killed)
      assert(tme.user == "egghead")
      assert(tme.message == "gibbed nescio")
    }
    "is not extracted when user doesn't match" in {
      LadderService
        .TimedUserMessageExtract(Function.const(None))
        .unapply(LadderParseTest.sampleMessage) shouldBe empty
    }
    "is not extracted for an empty input" in {
      LadderService
        .TimedUserMessageExtract(Function.const(None))
        .unapply("") shouldBe empty
    }
  }
}

object LadderParseTest {
  val sampleMessage = """2016-07-02T21:58:09 [92.21.240.78:egg] egg gibbed nescio"""
}
