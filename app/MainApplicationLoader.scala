import bali._
import bali.scala.make
import config.ConfigModule
import controllers._
import play.api.ApplicationLoader._
import play.api._
import play.api.inject.ApplicationLifecycle
import play.api.mvc._
import play.api.routing.Router
import play.filters.HttpFiltersComponents
import play.filters.gzip.GzipFilterComponents
import router.Routes

final class MainApplicationLoader extends ApplicationLoader {

  override def load(context: Context): Application = make[MainComponents].application
}

@Module
trait MainComponents
  extends AssetsComponents
    with BuiltInComponentsWithContext
    with ConfigModule
    with ControllerModule
    with GzipFilterComponents
    with HttpFiltersComponents {

  @Lookup
  val context: Context

  final override lazy val httpFilters: Seq[EssentialFilter] = super[HttpFiltersComponents].httpFilters :+ gzipFilter

  final override lazy val router: Router = new Routes(
    Assets_1 = assets,
    GameController_0 = gameController,
    errorHandler = httpErrorHandler,
  )
}

// TODO: Send PR to Play Framework with this:
trait BuiltInComponentsWithContext extends BuiltInComponents {

  val context: Context

  override def environment: Environment = context.environment

  override def devContext: Option[DevContext] = context.devContext

  override def applicationLifecycle: ApplicationLifecycle = context.lifecycle

  override def configuration: Configuration = context.initialConfiguration

  lazy val controllerComponents: ControllerComponents = DefaultControllerComponents(
    defaultActionBuilder,
    playBodyParsers,
    messagesApi,
    langs,
    fileMimeTypes,
    executionContext
  )
}
