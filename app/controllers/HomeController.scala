package controllers

import javax.inject._

import akka.stream.Materializer
import akka.stream.scaladsl.Source
import models.ConsoleGame
import play.api.http.ContentTypes
import play.api.libs.EventSource
import play.api.mvc._
import views.html._

@Singleton
class HomeController @Inject()(implicit materializer: Materializer) extends Controller {

  private val game = ConsoleGame(70, 240)

  private lazy val source = Source fromIterator (() => game iterator game.Board() map (game render _ substring 1))

  def index = Action { Ok(board()) }

  def boards = Action { Ok chunked (source via EventSource.flow) as ContentTypes.EVENT_STREAM }
}
