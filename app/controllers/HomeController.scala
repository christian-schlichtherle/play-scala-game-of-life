package controllers

import javax.inject._

import models.ConsoleGame
import play.api.mvc._
import views.html._

@Singleton
class HomeController extends Controller {

  private val game = ConsoleGame(20, 200)
  private val board2string = game.render
  import game._

  def index = Action { implicit request =>
    val text = (game iterator Board() take 10 map board2string).mkString
    Ok(main("Conway's Game of Life")(pre(text)))
  }
}
