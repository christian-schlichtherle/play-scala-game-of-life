package models

import models.Game.Setup

import scala.collection.immutable.BitSet

final case class Game(cols: Int, rows: Int) extends Grid {

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
}
