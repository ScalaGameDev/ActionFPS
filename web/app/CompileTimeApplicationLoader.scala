import akka.actor.ActorSystem
import com.actionfps.accumulation.ReferenceMapValidator
import com.actionfps.accumulation.user.GeoIpLookup
import com.actionfps.gameparser.enrichers.{IpLookup, MapValidator}
import com.softwaremill.macwire._
import controllers.{
  Admin,
  AllGames,
  ClansController,
  DownloadsController,
  EventStreamController,
  Forwarder,
  GamesController,
  IndexController,
  LadderController,
  MasterServerController,
  PlayersController,
  PlayersProvider,
  RawLogController,
  ServersController,
  StaticPageRouter,
  UserController,
  VersionController
}
import inters.IntersController
import lib.WebTemplateRender
import play.api.ApplicationLoader.Context
import play.api.Configuration
import play.api.cache.ehcache.EhCacheComponents
import play.api.http.FileMimeTypes
import play.api.libs.ws.WSClient
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.mvc.EssentialFilter
import play.filters.HttpFiltersComponents
import play.filters.cors.CORSComponents
import play.filters.gzip.GzipFilterComponents
import providers.ReferenceProvider
import providers.full.{
  FullProvider,
  FullProviderImpl,
  HazelcastCachedProvider,
  PlayersProviderImpl
}
import providers.games.GamesProvider
import router.Routes
import services._
import tl.ChallongeClient

final class CompileTimeApplicationLoader extends play.api.ApplicationLoader {
  def load(context: Context): play.api.Application =
    new CompileTimeApplicationLoaderComponents(context).application
}

final class CompileTimeApplicationLoaderComponents(context: Context)
    extends play.api.BuiltInComponentsFromContext(context)
    with HttpFiltersComponents
    with EhCacheComponents
    with GzipFilterComponents
    with AhcWSComponents
    with CORSComponents
    with _root_.controllers.AssetsComponents {

  override def httpFilters: Seq[EssentialFilter] = Seq(corsFilter, gzipFilter)
  implicit lazy val as: ActorSystem = this.actorSystem
  implicit lazy val mimeTypes: FileMimeTypes = this.fileMimeTypes
  implicit lazy val config: Configuration = this.configuration
  implicit lazy val ws: WSClient = this.wsClient
  lazy val env: play.Environment = this.environment.asJava
  lazy val webTemplateRender: WebTemplateRender = wire[WebTemplateRender]
  lazy val newsService: NewsService = wire[NewsService]
  lazy val allGames: AllGames = wire[AllGames]
  lazy val playersProvider: PlayersProvider = wire[PlayersProviderImpl]
  implicit lazy val referenceProvider: ReferenceProvider =
    new ReferenceProvider(configuration, defaultCacheApi)(wsClient,
                                                          executionContext)
  lazy val gamesProvider: GamesProvider = wire[GamesProvider]
  lazy val forwarder: Forwarder = wire[Forwarder]
  lazy val gamesController: GamesController = wire[GamesController]
  lazy val indexController: IndexController = wire[IndexController]
  implicit lazy val ipLookup: IpLookup = GeoIpLookup
  lazy val clansController: ClansController = wire[ClansController]
  lazy val playersController: PlayersController = wire[PlayersController]
  lazy val ladderController: LadderController = wire[LadderController]
  lazy val serversController: ServersController = wire[ServersController]
  lazy val pingerService: PingerService = wire[PingerService]
  lazy val admin: Admin = wire[Admin]
  lazy val versionController: VersionController = wire[VersionController]
  lazy val userController: UserController = wire[UserController]
  lazy val latestReleaseService: LatestReleaseService =
    wire[LatestReleaseService]
  lazy val downloadsController: DownloadsController = wire[DownloadsController]
  lazy val RawLogController: RawLogController =
    new RawLogController(configuration, controllerComponents)
  lazy val eventStreamController: EventStreamController =
    wire[EventStreamController]
  lazy val masterServerController: MasterServerController =
    wire[MasterServerController]
  lazy val intersController: IntersController =
    wire[IntersController]
  lazy val fullProvider: FullProvider = {
    val fullProviderImpl = wire[FullProviderImpl]
    if (useCached)
      new HazelcastCachedProvider(fullProviderImpl)(executionContext)
    else fullProviderImpl
  }
  private lazy val useCached =
    configuration
      .getOptional[String]("full.provider")
      .contains("hazelcast-cached")
  implicit lazy val mapValidator: MapValidator =
    ReferenceMapValidator.referenceMapValidator
  lazy val staticPageRouter: StaticPageRouter = wire[StaticPageRouter]
  lazy val prefix: String = "/"
  lazy val router: Routes = wire[Routes]
  lazy val challongeClient: ChallongeClient = wire[ChallongeClient]
  lazy val challongeEnabled: Boolean = configuration
    .get[Seq[String]]("play.modules.enabled")
    .contains("modules.ChallongeLoadModule")
  lazy val intersEnabled: Boolean = configuration
    .get[Seq[String]]("play.modules.enabled")
    .contains("modules.IntersLoadModule")
  val challongeServiceO: Option[ChallongeService] =
    if (challongeEnabled) Some(wire[ChallongeService]) else None
  lazy val intersService: IntersService =
    new IntersService(configuration)(() => referenceProvider.users,
                                     executionContext,
                                     wsClient,
                                     actorSystem)

  if (intersEnabled) intersService.beginPushing()

}
