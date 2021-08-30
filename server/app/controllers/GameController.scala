package controllers

import akka.stream.scaladsl._
import config.GameConfig
import controllers.GameController.close
import models.Game
import play.api.http.ContentTypes
import play.api.libs.EventSource.Event
import play.api.mvc._
import views.BoardRenderer

import scala.concurrent.duration._
import scala.util.chaining._

trait GameController extends BaseController {

  protected val config: GameConfig

  import config._

  def index(secs: Option[Int]): Action[AnyContent] = Action { implicit request =>
    secs.map { secs =>
      Ok(views.html.page(fps, secs))
    }.getOrElse {
      Redirect(routes.GameController.index(Some(config.secs)))
    }
  }

  def stream(cols: Int, rows: Int, secs: Int): Action[AnyContent] = Action {
    val game = Game(cols = cols, rows = rows)

    def render(prev: game.Board, next: game.Board) = Event[String](BoardRenderer(game)(prev, next))

    val boards = Source
      .fromIterator(() => game.start(setup))
      .sliding(3)
      .takeWhile(_.map(_.cells).pipe(s => s.size == s.distinct.size)) // not only blinkers
      .map(_.last)
      .sliding(2)
      .map { case Seq(prev, next) => render(prev, next) }
      .throttle(fps, 1.second)
      .takeWithin(secs.seconds)
    Ok.chunked(boards.concat(close), Some(ContentTypes.EVENT_STREAM))
  }
}

private object GameController {

  private val close = Source.single(Event(data = "close", id = None, name = Some("close")))
}
