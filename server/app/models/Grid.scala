package models

/** A grid has a number of rows and columns with iterable positions.
  * This trait is agnostic to the Game of Life itself.
  */
trait Grid {

  val cols: Int

  val rows: Int

  lazy val size: Int = rows * cols

  lazy val allPositions: Iterable[Position] = {
    for {
      col <- 0 until cols
      row <- 0 until rows
    } yield {
      Position(col, row)
    }
  }

  /** A position has a row, a column and an index with iterable neighbor positions. */
  final case class Position private(col: Int, row: Int) {

    val index: Int = row * cols + col

    lazy val neighborPositions: Iterable[Position] = {
      for {
        colOffset <- -1 to 1
        rowOffset <- -1 to 1
        if colOffset != 0 || rowOffset != 0
      } yield {
        Position(
          col = (cols + col + colOffset) % cols,
          row = (rows + row + rowOffset) % rows
        )
      }
    }
  }
}
