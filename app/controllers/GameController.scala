package controllers

import java.util.Locale
import javax.inject._

import akka.stream.scaladsl.{Flow, Source}
import com.typesafe.config.Config
import controllers.GameController._
import models.Game.SetupPredicate
import models.{ConsoleGame, Game}
import play.api.http.ContentTypes
import play.api.libs.EventSource.Event
import play.api.mvc._

import scala.reflect.runtime.currentMirror
import scala.tools.reflect.ToolBox

@Singleton
class GameController @Inject()(context: Context) extends Controller {

  private lazy val closeEvent = Source single Event(data = "close", id = None, name = Some("close"))

  def boards: Action[AnyContent] = Action { Ok(views.html.boards(context)) }

  def stream(context: Context): Action[AnyContent] = {
    import context._
    Action {
      val game = ConsoleGame(rows, columns)
      val boardEvents = {
        Source
          .fromIterator(() => game iterator game.Board(setup))
          .take(generations)
          .map(game render _ substring 1)
          .map(Event(_))
      }
      val events = boardEvents concat closeEvent
      Ok chunked (events via Flow[Event]) as ContentTypes.EVENT_STREAM
    }
  }
}

object GameController {

  case class Context(rows: Int, columns: Int, generations: Int, setup: SetupPredicate = Game.random) {

    require(rows >= 2)
    require(columns >= 2)
    require(generations >= 1)
  }

  object Context {

    implicit def queryStringBindable(implicit intBinder: QueryStringBindable[Int]): QueryStringBindable[Context] = {
      new QueryStringBindable[Context] {

        def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Context]] = {
          for {
            rows <- intBinder.bind(key + ".rows", params)
            columns <- intBinder.bind(key + ".columns", params)
            generations <- intBinder.bind(key + ".generations", params)
          } yield {
            (rows, columns, generations) match {
              case (Right(r), Right(c), Right(g)) => Right(Context(rows = r, columns = c, generations = g))
              case _ => Left("Unable to bind a Context.")
            }
          }
        }

        override def unbind(key: String, context: Context): String = {
          import context._
          intBinder.unbind(key + ".rows", rows) + "&" +
            intBinder.unbind(key + ".columns", columns) + "&" +
            intBinder.unbind(key + ".generations", generations)
        }
      }
    }

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
