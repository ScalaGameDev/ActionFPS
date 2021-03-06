package af.inters

import java.time.Instant

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.actionfps.inter.{InterOut, UserMessage}
import com.actionfps.servers.ValidServers
import org.scalatest.{DoNotDiscover, WordSpec}
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global

/**
  * Created by william on 28/1/17.
  */
@DoNotDiscover
class DiscordInterTest extends WordSpec {

  private implicit lazy val actorSystem: ActorSystem = ActorSystem()
  private implicit lazy val actorMaterializer: ActorMaterializer =
    ActorMaterializer()
  private implicit lazy val wsClient: AhcWSClient = AhcWSClient()
  "It" must {
    "work" ignore {
      val hookUrl: String = ???
      val discordInters = DiscordInters(hookUrl)
      val fr = discordInters.pushInterOut(
        InterOut.apply(UserMessage(
          instant = Instant.now(),
          serverId = ValidServers.validServers
            .filter(_.address.nonEmpty)
            .drop(2)
            .head
            .logId,
          ip = "123",
          userId = "sanzo",
          nickname = "w00p|Sanzo",
          messageText = "!inter"
        )))
      val r = Await.result(fr, 10.seconds)
      info(s"$r, ${r.get.body}")
    }
  }

}
