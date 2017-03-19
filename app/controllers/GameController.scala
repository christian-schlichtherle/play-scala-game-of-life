package controllers

import javax.inject._

import akka.stream.scaladsl.{Flow, Source}
import models.Game
import play.api.http.ContentTypes
import play.api.libs.EventSource.Event
import play.api.mvc._
import views.Game2String
import GameController._

@Singleton
class GameController @Inject()(default: Game) extends Controller {

  def boards(game: Option[Game]): Action[AnyContent] = {
    Action {
      game map { game =>
        Ok(views.html.boards(game))
      } getOrElse {
        Redirect(routes.GameController.boards(Some(default)))
      }
    }
  }

  def stream(game: Game): Action[AnyContent] = {
    Action {
      val view = Game2String(game) _ andThen (_ substring 1) andThen Event[String]
      val boardEvents = Source fromIterator (() => game.iterator) map view
      val events = boardEvents concat closeEvent
      Ok chunked (events via Flow[Event]) as ContentTypes.EVENT_STREAM
    }
  }
}

private object GameController {

  private lazy val closeEvent = Source single Event(data = "close", id = None, name = Some("close"))
}
