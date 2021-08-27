package views

import models.Game

object BoardRenderer {

  private val EmptyCell = 'E'
  private val BornCell = 'B'
  private val LiveCell = 'L'
  private val PartyCell = 'P'
  private val DeadCell = 'D'
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
        append {
          prev.alive(p) -> next.alive(p) match {
            case false -> false => EmptyCell
            case false -> true => BornCell
            case true -> false => DeadCell
            case true -> true => if (next.neighborCount(p) >= 3) PartyCell else LiveCell
          }
        }
      }
      append(LineSeparator)
    }
    builder.toString.ensuring(_.length == length)
  }
}
