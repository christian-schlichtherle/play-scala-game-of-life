package controllers

import akka.stream.scaladsl._
import bali.Lookup
import controllers.GameController.close
import models.Game
import play.api.Configuration
import play.api.http.ContentTypes
import play.api.libs.EventSource.Event
import play.api.mvc._
import views.BoardRenderer

import scala.concurrent.duration._
import scala.util.chaining._

trait GameController extends BaseController {

  protected def game(config: Configuration): Game

  @Lookup("gameConfig")
  protected val config: Configuration

  import config._

  def boards(game: Option[Game]): Action[AnyContent] = Action {
    game.map { game =>
      Ok(views.html.boards(game))
    }.getOrElse {
      Redirect(routes.GameController.boards(Some(this.game(config))))
    }
  }

  def stream(game: Game): Action[AnyContent] = Action {
    def view(prev: game.Board, next: game.Board) = Event[String](BoardRenderer(game)(prev, next))

    val events = Source
      .fromIterator(() => game.iterator)
      .sliding(3)
      .takeWhile(_.map(_.cells).pipe(s => s.size == s.distinct.size)) // not only blinkers
      .map(_.head)
      .sliding(2)
      .map { case Seq(prev, next) => view(prev, next) }
      .throttle(get[Int]("fps"), 1.second)
      .takeWithin(get[Int]("secs").seconds)
    Ok.chunked(events.concat(close), Some(ContentTypes.EVENT_STREAM))
  }
}

private object GameController {

  private val close = Source.single(Event(data = "close", id = None, name = Some("close")))
}
