import _root_.controllers._
import bali.Module
import bali.scala.make
import models.ModelModule
import play.api.ApplicationLoader.Context
import play.api._
import play.api.routing.Router
import router.Routes

final class MainApplicationLoader extends ApplicationLoader {

  override def load(context: Context): Application = new MainComponents(context).application
}

final class MainComponents(context: Context)
  extends BuiltInComponentsFromContext(context)
    with AssetsComponents
    with NoHttpFiltersComponents {

  lazy val module: MainModule = make[MainModule]
  import module._

  override lazy val router: Router = new Routes(
    Assets_1 = assets,
    GameController_0 = gameController,
    errorHandler = httpErrorHandler,
  )
}

@Module
trait MainModule extends ControllerModule with ModelModule
