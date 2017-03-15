package models

import models.Grid._

/** A grid has a number of rows and columns with iterable positions.
  * This trait is agnostic to the Game of Life itself.
  */
trait Grid {

  val rows: Int

  val columns: Int

  @transient
  lazy val size: Int = rows * columns

  @transient
  lazy val allPositions: Iterable[Position] = {
    for {
      row <- allRows
      column <- allColumns
    } yield {
      Position(row, column)
    }
  }

  @inline
  private def allRows = Iterable.range(0, rows)

  @inline
  private def allColumns = Iterable.range(0, columns)

  /** A position has a row, a column and an index with iterable neighbor positions. */
  case class Position private(row: Int, column: Int) {

    @transient
    lazy val index: Int = row * columns + column

    @transient
    lazy val allNeighborPositions: Iterable[Position] = {
      for {
        rowOffset <- allNeighborOffsets
        columnOffset <- allNeighborOffsets
        if rowOffset != 0 || columnOffset != 0
      } yield {
        Position(
          row = (rows + row + rowOffset) % rows,
          column = (columns + column + columnOffset) % columns
        )
      }
    }
  }
}

private object Grid {

  private val allNeighborOffsets = Iterable.range(-1, 2)
}
