package controllers

import com.typesafe.config.ConfigFactory
import org.scalatest.FunSuite
import play.api.Configuration
import org.scalatest.OptionValues._
import org.scalatest.Matchers._

/**
  * Created by me on 29/07/2016.
  */
class LadderControllerTest extends FunSuite {

  test("Config parsing works") {
    val config =
      """
        | items = [{
        |year = 2016
        |command = ["/aff"]
        | }]
        |
      """.stripMargin

    val configuration = Configuration.apply(ConfigFactory.parseString(config))

    val l = LadderController.getSourceCommands(configuration, "items").value
    l should have size 1
    l.head._2 shouldBe 2016
    l.head._1.head shouldBe "/aff"

  }

}
