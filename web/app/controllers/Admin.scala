package controllers

/**
  * Created by William on 05/12/2015.
  */

import javax.inject._
import com.actionfps.accumulation.GameAxisAccumulator$
import play.api.Configuration
import play.api.mvc.{Action, Controller}
import providers.ReferenceProvider
import providers.full.FullProvider

@Singleton
class Admin @Inject()(fullProvider: FullProvider,
                      referenceProvider: ReferenceProvider,
                      configuration: Configuration)
  extends Controller {

  /**
    * Reloads reference data and forces [[FullProvider]] to reevaluate usernames and clans for all games.
    * @return
    */
  def reloadReference = Action { request =>
    val apiKeyO = request.getQueryString("api-key").orElse(request.headers.get("api-key"))
    val apiKeyCO = configuration.getString("af.admin-api-key")
    if (apiKeyO == apiKeyCO) {
      refresh()
      Ok("Done reloading")
    } else {
      Forbidden("Wrong api key.")
    }
  }

  def refresh(): Unit = {
    referenceProvider.unCache()
    fullProvider.reloadReference()
  }
}
