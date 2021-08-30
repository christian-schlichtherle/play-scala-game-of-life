package views

import models.Game

object BoardRenderer {

  private object Cell {
    val Cool = 'C'
    val Fresh = 'F'
    val Ghost = 'G'
    val OMG = 'O'
    val Party = 'P'
    val Skull = 'S'
  }
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
          import Cell._
          prev.alive(p) -> next.alive(p) match {
            case false -> false => Ghost
            case false -> true => Fresh
            case true -> false => Skull
            case true -> true => {
              next.neighborCount(p) match {
                case 2 => Cool
                case 3 => Party
                case _ => OMG
              }
            }
          }
        }
      }
      append(LineSeparator)
    }
    builder.toString.ensuring(_.length == length)
  }
}
