package views

import models.Game

object BoardRenderer {

  private val BornCell = '+'
  private val LiveCell = '*'
  private val DeadCell = ' '
  private val LineSeparator = '\n'

  def apply(game: Game)(prev: game.Board, next: game.Board): String = {
    import game._

    val allRows = 0 until rows
    val allColumns = 0 until cols
    val length = rows * (cols + 1)
    val builder = new StringBuilder(length)

    import builder._

    allRows.foreach { row =>
      allColumns.foreach { column =>
        val p = Position(column, row)
        append(if (!next.alive(p)) DeadCell else if (prev.alive(p)) LiveCell else BornCell)
      }
      append(LineSeparator)
    }
    builder.toString.ensuring(_.length == length)
  }
}
