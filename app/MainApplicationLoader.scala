import _root_.controllers._
import bali.Module
import bali.scala.make
import config.ConfigModule
import play.api.ApplicationLoader.Context
import play.api._
import play.api.mvc.EssentialFilter
import play.api.routing.Router
import play.filters.HttpFiltersComponents
import play.filters.gzip.GzipFilterComponents
import router.Routes

final class MainApplicationLoader extends ApplicationLoader {

  override def load(context: Context): Application = new MainComponents(context).application
}

final class MainComponents(context: Context)
  extends BuiltInComponentsFromContext(context)
    with AssetsComponents
    with GzipFilterComponents
    with HttpFiltersComponents {

  override def httpFilters: Seq[EssentialFilter] = {
    Seq(csrfFilter, gzipFilter, securityHeadersFilter, allowedHostsFilter)
  }

  lazy val module: MainModule = make[MainModule]

  import module._

  override lazy val router: Router = new Routes(
    Assets_1 = assets,
    GameController_0 = gameController,
    errorHandler = httpErrorHandler,
  )
}

@Module
trait MainModule extends ConfigModule with ControllerModule
