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

final case class Game(cols: Int, rows: Int, secs: Int, setup: SetupPredicate) extends Grid {

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
      neighborCount(position) match {
        case 2 => alive(position)
        case 3 => true
        case _ => false
      }
    }

    def neighborCount(position: Position): Int = position.neighborPositions.count(alive)

    def alive(position: Position): Boolean = cells(position.index)
  }

  object Board {

    def apply(): Board = {
      val cells = allPositions.filter(p => setup(p.col, p.row)).foldLeft(BitSet.empty)(_ + _.index)
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
      secs = get[Int]("secs"),
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
        secs <- intBinder.bind("secs", params)
      } yield {
        for {
          c <- cols
          r <- rows
          s <- secs
          game <- Try(Game(cols = c, rows = r, secs = s, setup = random)).toEither.left.map(_.toString)
        } yield {
          game
        }
      }
    }

    override def unbind(key: String, value: Game): String = {
      import value._
      intBinder.unbind("cols", cols) + "&" + intBinder.unbind("rows", rows) + "&" + intBinder.unbind("secs", secs)
    }
  }

  def random(col: Int, row: Int): Boolean = ThreadLocalRandom.current.nextBoolean()

  def blinkers(col: Int, row: Int): Boolean = row % 4 == 1 && col % 4 < 3
}
