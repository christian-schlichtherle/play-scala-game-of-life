package models

import java.util.Locale
import java.util.concurrent.ThreadLocalRandom

import com.typesafe.config.Config
import models.Game.SetupPredicate
import play.api.mvc.QueryStringBindable

import scala.collection.BitSet

import scala.reflect.runtime.currentMirror
import scala.tools.reflect.ToolBox

case class Game(rows: Int, columns: Int, generations: Option[Int], setup: SetupPredicate = Game.random) extends Grid with Equals {

  require(rows >= 2)
  require(columns >= 2)
  generations foreach (g => require(g > 0))

  def iterator: Iterator[Board] = {
    val it = Iterator.iterate(Board())(_.next)
    generations map it.take getOrElse it
  }

  case class Board private(cells: BitSet, generation: Int) {

    def next: Board = {
      copy(
        cells = (BitSet.empty /: (allPositions filter nextAlive)) (_ + _.index),
        generation = generation + 1
      )
    }

    private def nextAlive(position: Position) = {
      position.allNeighborPositions count alive match {
        case 2 => alive(position)
        case 3 => true
        case _ => false
      }
    }

    def alive(position: Position): Boolean = cells(position.index)
  }

  object Board {

    def apply(): Board = {
      val cells = (BitSet.empty /: (allPositions filter (p => setup(p.row, p.column)))) (_ + _.index)
        .ensuring(cells => cells.isEmpty || (0 <= cells.min && cells.max < size))
      new Board(cells, 1)
    }
  }
}

object Game {

  type SetupPredicate = (Int, Int) => Boolean

  def apply(config: Config): Game = {
    import config._

    def setup: SetupPredicate = {
      getString("setup") toLowerCase Locale.ENGLISH match {
        case "random" => Game.random
        case "blinkers" => Game.blinkers
        case other => evaluate("($r: Int, $c: Int) => { " + other + " }: Boolean")
      }
    }

    Game(
      rows = getInt("rows"),
      columns = getInt("columns"),
      generations = Some(getInt("generations")) filter (_ > 0),
      setup = setup
    )
  }

  private def evaluate[A](string: String): A = {
    val tb = currentMirror mkToolBox ()
    val tree = tb parse string
    (tb eval tree).asInstanceOf[A]
  }

  implicit val binder: QueryStringBindable[Game] = {
    new QueryStringBindable[Game] {

      private val intBinder = implicitly[QueryStringBindable[Int]]
      private val optionIntBinder = implicitly[QueryStringBindable[Option[Int]]]

      def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Game]] = {
        for {
          rows <- intBinder.bind(key + ".rows", params)
          columns <- intBinder.bind(key + ".columns", params)
          generations <- optionIntBinder.bind(key + ".generations", params)
        } yield {
          (rows, columns, generations) match {
            case (Right(r), Right(c), Right(g)) => Right(Game(rows = r, columns = c, generations = g filter (_ > 0)))
            case _ => Left("Unable to bind a Context.")
          }
        }
      }

      override def unbind(key: String, context: Game): String = {
        import context._
        intBinder.unbind(key + ".rows", rows) + "&" +
          intBinder.unbind(key + ".columns", columns) +
          (generations map ("&" + intBinder.unbind(key + ".generations", _)) getOrElse "")
      }
    }
  }

  def random(row: Int, column: Int): Boolean = ThreadLocalRandom.current nextBoolean ()

  def blinkers(row: Int, column: Int): Boolean = row % 4 == 1 && column % 4 < 3
}
