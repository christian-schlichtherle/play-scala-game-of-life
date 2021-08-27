package models

import models.Game.Setup
import play.api.Configuration
import play.api.mvc.QueryStringBindable

import scala.collection.immutable.BitSet
import scala.util.Try

final case class Game(cols: Int, rows: Int, secs: Int) extends Grid {

  require(rows >= 2)
  require(cols >= 2)

  def start(setup: Setup): Iterator[Board] = Iterator.iterate(Board(setup))(_.next)

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

  private object Board {

    def apply(setup: Setup): Board = {
      val cells = allPositions.filter(p => setup(p.col, p.row)).foldLeft(BitSet.empty)(_ + _.index)
        .ensuring(cells => cells.isEmpty || (0 <= cells.min && cells.max < size))
      new Board(cells, 1)
    }
  }
}

object Game {

  type Setup = (Int, Int) => Boolean

  def apply(config: Configuration): Game = {
    import config._
    Game(
      cols = get[Int]("cols"),
      rows = get[Int]("rows"),
      secs = get[Int]("secs"),
    )
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
          game <- Try(Game(cols = c, rows = r, secs = s)).toEither.left.map(_.toString)
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
}
