package controllers

import javax.inject._

import akka.actor.ActorSystem
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Source
import com.actionfps.clans.ClanNamer
import lib.KeepAliveEvents
import play.api.mvc.{Action, Controller}
import providers.ReferenceProvider
import providers.full.NewClanwarCompleted
import providers.games.NewGamesProvider
import services.PingerService

import scala.async.Async.{async, await}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EventStreamController @Inject()(pingerService: PingerService,
                                      referenceProvider: ReferenceProvider)
                                     (implicit actorSystem: ActorSystem,
                                      executionContext: ExecutionContext) extends Controller {

  private def namerF: Future[ClanNamer] = async {
    val clans = await(referenceProvider.clans)
    ClanNamer(id => clans.find(_.id == id).map(_.name))
  }

  private val clanwarsSource = {
    namerF.map { implicit namer =>
      Source
        .actorRef[NewClanwarCompleted](10, OverflowStrategy.dropBuffer)
        .mapMaterializedValue(actorSystem.eventStream.subscribe(_, classOf[NewClanwarCompleted]))
        .map(_.toEvent)
    }
  }


  def eventStream = Action.async {
    async {
      Ok.chunked(
        content = {
          pingerService
            .liveGamesWithRetainedSource
            .merge(NewGamesProvider.newGamesSource)
            .merge(await(clanwarsSource))
            .merge(KeepAliveEvents.source)
        }
      )
    }
  }

  def serverUpdates = Action {
    Ok.chunked(
      content = pingerService.liveGamesWithRetainedSource.merge(KeepAliveEvents.source)
    ).as("text/event-stream")
  }

  def newGames = Action {
    Ok.chunked(
      content = NewGamesProvider.newGamesSource.merge(KeepAliveEvents.source)
    ).as("text/event-stream")
  }

}
