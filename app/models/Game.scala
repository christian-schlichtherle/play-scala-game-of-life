package models

import models.Game.SetupPredicate
import play.api.Configuration
import play.api.mvc.QueryStringBindable

import java.util.Locale
import java.util.concurrent.ThreadLocalRandom
import scala.collection.immutable.BitSet
import scala.reflect.runtime.currentMirror
import scala.tools.reflect.ToolBox
import scala.util.Try

final case class Game(cols: Int, rows: Int, setup: SetupPredicate = Game.random) extends Grid {

  require(rows >= 2)
  require(cols >= 2)

  def iterator: Iterator[Board] = Iterator.iterate(Board())(_.next)

  final case class Board private(cells: BitSet, generation: Int) {

    def next: Board = {
      copy(
        cells = allPositions.filter(nextAlive).foldLeft(BitSet.empty)(_ + _.index),
        generation = generation + 1
      )
    }

    private def nextAlive(position: Position) = {
      position.allNeighborPositions.count(alive) match {
        case 2 => alive(position)
        case 3 => true
        case _ => false
      }
    }

    def alive(position: Position): Boolean = cells(position.index)
  }

  object Board {

    def apply(): Board = {
      val cells = allPositions.filter(p => setup(p.row, p.col)).foldLeft(BitSet.empty)(_ + _.index)
        .ensuring(cells => cells.isEmpty || (0 <= cells.min && cells.max < size))
      new Board(cells, 1)
    }
  }
}

object Game {

  type SetupPredicate = (Int, Int) => Boolean

  def apply(config: Configuration): Game = {
    import config._

    def setup: SetupPredicate = {
      get[String]("setup").toLowerCase(Locale.ENGLISH) match {
        case "random" => Game.random
        case "blinkers" => Game.blinkers
        case other => evaluate("($r: Int, $c: Int) => { " + other + " }: Boolean")
      }
    }

    Game(
      cols = get[Int]("cols"),
      rows = get[Int]("rows"),
      setup = setup
    )
  }

  private def evaluate[A](string: String): A = {
    val tb = currentMirror.mkToolBox()
    val tree = tb.parse(string)
    tb.eval(tree).asInstanceOf[A]
  }

  implicit object gameBinder extends QueryStringBindable[Game] {

    private val intBinder = implicitly[QueryStringBindable[Int]]

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Game]] = {
      for {
        cols <- intBinder.bind("cols", params)
        rows <- intBinder.bind("rows", params)
      } yield {
        for {
          c <- cols
          r <- rows
          game <- Try(Game(cols = c, rows = r)).toEither.left.map(_.toString)
        } yield {
          game
        }
      }
    }

    override def unbind(key: String, value: Game): String = {
      import value._
      intBinder.unbind("cols", cols) + "&" + intBinder.unbind("rows", rows)
    }
  }

  def random(row: Int, column: Int): Boolean = ThreadLocalRandom.current.nextBoolean()

  def blinkers(row: Int, column: Int): Boolean = row % 4 == 1 && column % 4 < 3
}
