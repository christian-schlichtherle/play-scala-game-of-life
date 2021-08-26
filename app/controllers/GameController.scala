package controllers

import akka.stream.scaladsl._
import models.Game
import play.api.http.ContentTypes
import play.api.libs.EventSource.Event
import play.api.mvc._
import views.Game2String

import javax.inject._

@Singleton
class GameController @Inject()(default: Game, cc: ControllerComponents) extends AbstractController(cc) {

  def boards(game: Option[Game]): Action[AnyContent] = {
    Action {
      game.map { game =>
        Ok(views.html.boards(game))
      }.getOrElse {
        Redirect(routes.GameController.boards(Some(default)))
      }
    }
  }

  def stream(game: Game): Action[AnyContent] = {
    Action {
      val view = (Game2String(game) _).andThen(_.substring(1)).andThen(Event[String])
      val events = Source.fromIterator(() => game.iterator).map(view)
      Ok.chunked(events.via(Flow[Event])).as(ContentTypes.EVENT_STREAM)
    }
  }
}
