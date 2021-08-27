package controllers

import akka.stream.scaladsl._
import controllers.GameController.close
import models.Game
import models.Game.Setup
import play.api.Configuration
import play.api.http.ContentTypes
import play.api.libs.EventSource.Event
import play.api.mvc._
import views.BoardRenderer

import scala.concurrent.duration._
import scala.util.chaining._

trait GameController extends BaseController {

  protected val config: Configuration

  protected val setup: Setup

  import config._

  def boards(game: Option[Game]): Action[AnyContent] = Action {
    game.map { game =>
      Ok(views.html.boards(game))
    }.getOrElse {
      Redirect(routes.GameController.boards(Some(Game(config))))
    }
  }

  def stream(game: Game): Action[AnyContent] = Action {
    def render(prev: game.Board, next: game.Board) = Event[String](BoardRenderer(game)(prev, next))

    val boards = Source
      .fromIterator(() => game.start(setup))
      .sliding(3)
      .takeWhile(_.map(_.cells).pipe(s => s.size == s.distinct.size)) // not only blinkers
      .map(_.head)
      .sliding(2)
      .map { case Seq(prev, next) => render(prev, next) }
      .throttle(get[Int]("fps"), 1.second)
      .takeWithin(game.secs.seconds)
    Ok.chunked(boards.concat(close), Some(ContentTypes.EVENT_STREAM))
  }
}

private object GameController {

  private val close = Source.single(Event(data = "close", id = None, name = Some("close")))
}
