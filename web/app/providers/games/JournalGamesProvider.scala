package providers.games

/**
  * Created by William on 01/01/2016.
  */

import java.io.File
import java.util.concurrent.Executors
import javax.inject._

import af.streamreaders.{IteratorTailerListenerAdapter, Scanner, TailedScannerReader}
import akka.agent.Agent
import com.actionfps.accumulation.GeoIpLookup
import com.actionfps.accumulation.ValidServers.ImplicitValidServers._
import com.actionfps.accumulation.ValidServers.Validator._
import com.actionfps.api.Game
import com.actionfps.gameparser.enrichers._
import org.apache.commons.io.input.Tailer
import play.api.inject.ApplicationLifecycle
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.{Configuration, Logger}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future, blocking}
import com.actionfps.formats.json.Formats._
import com.actionfps.gameparser.GameScanner

object JournalGamesProvider {

  def getJournalGames(logger: Logger, file: File): List[Game] = {
    val src = scala.io.Source.fromFile(file)
    try src.getLines().scanLeft(GameScanner.initial)(GameScanner.scan).collect(GameScanner.collect).toList
    finally src.close()
  }

  def getGamesSnapshot(logger: Logger, file: File): List[Game] = {
    val src = scala.io.Source.fromFile(file)
    try src.getLines().filter(_.nonEmpty).map { line =>
      line.split("\t").toList match {
        case id :: _ :: _ :: jsonText :: Nil =>
          Json.fromJson[Game](Json.parse(jsonText)) match {
            case JsSuccess(good, _) => good
            case e: JsError =>
              throw new RuntimeException(s"Failed to parse JSON in ${file}: $jsonText")
          }
        case _ =>
          throw new RuntimeException(s"Failed to parse line in ${file}: $line")
      }
    }.toList.filter(_.validate.isRight).filter(_.validateServer)
    finally src.close
  }

}

/**
  * Load in the list of journals - and tail the last one to grab the games.
  */
@Singleton
class JournalGamesProvider @Inject()(configuration: Configuration,
                                     applicationLifecycle: ApplicationLifecycle)
                                    (implicit executionContext: ExecutionContext)
  extends GamesProvider {

  val logger = Logger(getClass)

  val hooks = Agent(Set.empty[JsonGame => Unit])

  override def addHook(f: (JsonGame) => Unit): Unit = hooks.send(_ + f)

  override def removeHook(f: (JsonGame) => Unit): Unit = hooks.send(_ - f)

  private val journalFiles = configuration.underlying.getStringList("af.journal.paths").asScala.map(new File(_))
  private val gamesDatas = configuration.underlying.getStringList("af.ladder.games-data").asScala.map(new File(_))

  implicit private val geoIp = GeoIpLookup
  private val ex = Executors.newFixedThreadPool(journalFiles.size + 1)
  applicationLifecycle.addStopHook(() => Future(ex.shutdown()))

  val gamesDataF = Future {
    blocking {
      gamesDatas.par.flatMap { file =>
        JournalGamesProvider.getGamesSnapshot(logger, file)
      }.map(g => g.id -> g.withGeo.flattenPlayers).toList.toMap
    }
  }

  private val gamesA = for {d <- gamesDataF; d <- gamesI(d)} yield d

  private def gamesI(input: Map[String, JsonGame]) = Future {
    blocking {
      val (initialGames, tailIterator) = journalFiles.toList.sortBy(_.lastModified()).reverse match {
        case recent :: rest =>
          val adapter = new IteratorTailerListenerAdapter()
          val tailer = new Tailer(recent, adapter, 2000)
          val reader = TailedScannerReader(adapter, Scanner(GameScanner.initial)(GameScanner.scan))
          ex.submit(tailer)
          applicationLifecycle.addStopHook(() => Future(tailer.stop()))
          val (initialRecentGames, tailIterator) = reader.collect(GameScanner.collect)
          val otherGames = rest.par.map { file =>
            JournalGamesProvider.getJournalGames(logger, file)
          }.toList.flatten
          (otherGames ++ initialRecentGames, tailIterator)
        case Nil =>
          (List.empty, Iterator.empty)
      }
      val jigm = initialGames.filter(_.validate.isRight).filter(_.validateServer).map(g => g.id -> g.withGeo).toMap
      val gamesAgent = Agent(jigm ++ input)
      ex.submit(new Runnable {
        override def run(): Unit = {
          tailIterator.foreach { game =>
            if (game.validate.isRight && game.validateServer) {
              val gg = game.withGeo.flattenPlayers
              gamesAgent.send(_.updated(gg.id, gg))
              hooks.get().foreach(h => h(gg))
            }
          }
        }
      })
      gamesAgent
    }
  }

  override def games: Future[Map[String, JsonGame]] = gamesA.map(_.get())

}
