package controllers

import java.util.Locale
import javax.inject._

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}
import com.typesafe.config.Config
import controllers.GameController._
import models.Game.SetupPredicate
import models.{ConsoleGame, Game}
import play.api.http.ContentTypes
import play.api.libs.EventSource
import play.api.libs.EventSource.Event
import play.api.mvc._

import scala.reflect.runtime.currentMirror
import scala.tools.reflect.ToolBox

@Singleton
class GameController @Inject()(context: Context) extends Controller {

  import context._

  private val game = ConsoleGame(rows, columns)

  private lazy val events = boardEvents concat closeEvent

  private def boardEvents = {
    Source
      .fromIterator(() => game iterator game.Board(setup))
      .take(generations)
      .map(game render _ substring 1)
      .map(Event(_))
  }

  private def closeEvent = Source single Event(data = "close", id = None, name = Some("close"))

  def boards: Action[AnyContent] = Action { Ok(views.html.boards()) }

  def stream: Action[AnyContent] = {
    Action { Ok chunked (events via Flow[Event]) as ContentTypes.EVENT_STREAM }
  }
}

object GameController {

  case class Context(rows: Int, columns: Int, generations: Int, setup: SetupPredicate) {

    require(rows >= 2)
    require(columns >= 2)
    require(generations >= 1)
  }

  object Context {

    def apply(config: Config): Context = {
      import config._

      def setup: SetupPredicate = {
        getString("setup") toLowerCase Locale.ENGLISH match {
          case "random" => Game.random
          case "blinkers" => Game.blinkers
          case other => eval("($r: Int, $c: Int) => { " + other + " }: Boolean")
        }
      }

      Context(
        rows = getInt("rows"),
        columns = getInt("columns"),
        generations = getInt("generations"),
        setup = setup
      )
    }

    private def eval[A](string: String): A = {
      val tb = currentMirror mkToolBox ()
      val tree = tb parse string
      (tb eval tree).asInstanceOf[A]
    }
  }
}
