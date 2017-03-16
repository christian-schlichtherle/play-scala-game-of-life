package controllers

import javax.inject._

import akka.stream.Materializer
import akka.stream.scaladsl.Source
import models.ConsoleGame
import play.api.http.ContentTypes
import play.api.libs.Comet
import play.api.mvc._
import views.html._

@Singleton
class HomeController @Inject()(implicit materializer: Materializer) extends Controller {

  private val game = ConsoleGame(70, 240)

  private val board2string = game.render andThen (_ substring 1)

  private lazy val source = Source.fromIterator(() => game iterator game.Board() map board2string)

  def index = Action { Ok(board()) }

  def comet = Action { Ok.chunked(source via (Comet string "parent.setBoardHtml")) as ContentTypes.HTML }
}
