package controllers

import akka.stream.scaladsl._
import controllers.GameController.closeEvent
import models.Game
import play.api.Configuration
import play.api.http.ContentTypes
import play.api.libs.EventSource.Event
import play.api.mvc._
import views.BoardRenderer

import javax.inject._
import scala.concurrent.duration._

@Singleton
class GameController @Inject()(@Named("game") config: Configuration, default: Provider[Game], cc: ControllerComponents) extends AbstractController(cc) {

  def boards(game: Option[Game]): Action[AnyContent] = Action {
    game.map { game =>
      Ok(views.html.boards(game))
    }.getOrElse {
      Redirect(routes.GameController.boards(Some(default.get)))
    }
  }

  def stream(game: Game): Action[AnyContent] = Action {
    def view(prev: game.Board, next: game.Board) = Event[String](BoardRenderer(game)(prev, next))
    import config._
    val events = Source
      .fromIterator(() => game.iterator)
      .sliding(3)
      .takeWhile(seq => seq.map(_.cells).toSet.size == seq.size) // detect blinkers
      .map(_.head)
      .sliding(2)
      .map { case Seq(prev, next) => view(prev, next) }
      .throttle(get[Int]("fps"), 1.second)
      .takeWithin(get[Int]("secs").seconds)
    Ok.chunked(events.concat(closeEvent), Some(ContentTypes.EVENT_STREAM))
  }
}

private object GameController {

  private lazy val closeEvent = Source single Event(data = "close", id = None, name = Some("close"))
}
