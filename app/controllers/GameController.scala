package controllers

import javax.inject._

import akka.stream.scaladsl.{Flow, Source}
import models.Game
import play.api.http.ContentTypes
import play.api.libs.EventSource.Event
import play.api.mvc._
import views.Game2String

@Singleton
class GameController @Inject()(game: Game) extends Controller {

  private lazy val closeEvent = Source single Event(data = "close", id = None, name = Some("close"))

  def boards: Action[AnyContent] = Action { Ok(views.html.boards(game)) }

  def stream(game: Game): Action[AnyContent] = {
    Action {
      val view = Game2String(game) _ andThen (_ substring 1) andThen Event[String]
      val boardEvents = Source fromIterator (() => game.iterator) map view
      val events = boardEvents concat closeEvent
      Ok chunked (events via Flow[Event]) as ContentTypes.EVENT_STREAM
    }
  }
}
