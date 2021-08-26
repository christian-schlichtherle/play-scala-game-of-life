package models

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
      row <- 0.until(rows)
      column <- 0.until(columns)
    } yield {
      Position(row, column)
    }
  }

  /** A position has a row, a column and an index with iterable neighbor positions. */
  case class Position private(row: Int, column: Int) {

    @transient
    val index: Int = row * columns + column

    @transient
    lazy val allNeighborPositions: Iterable[Position] = {
      for {
        rowOffset <- -1 to 1
        columnOffset <- -1 to 1
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
