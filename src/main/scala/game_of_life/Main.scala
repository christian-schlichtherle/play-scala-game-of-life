package game_of_life

import game_of_life.Game.SetupPredicate

import scala.Console._
import scala.reflect.runtime.currentMirror
import scala.tools.reflect.ToolBox

object Main extends App {

  private def eval[A](string: String): A = {
    val tb = currentMirror mkToolBox ()
    val tree = tb parse string
    (tb eval tree).asInstanceOf[A]
  }

  val rows = args(0).toInt
  val columns = args(1).toInt
  val generations = {
    args(2) match {
      case "-" => Int.MaxValue
      case x => x.toInt
    }
  }
  val setup: SetupPredicate = {
    args drop 3 match {
      case Array() => Game.random
      case Array(word) if word equalsIgnoreCase "blinkers" => Game.blinkers
      case array => eval("($r: Int, $c: Int) => { " + (array mkString " ") + " }: Boolean")
    }
  }
  val game = ConsoleGame(rows, columns)
  val render = game.render
  import game._
  for (board <- iterator(Board(setup)) take generations) {
    print(render(board))
    flush()
  }
}
