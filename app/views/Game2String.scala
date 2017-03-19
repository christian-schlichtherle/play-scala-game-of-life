package views

import models.Game

object Game2String {

  private val horizontal = '─'
  private val vertical = '│'
  private val downAndRight = '┌'
  private val downAndLeft = '┐'
  private val upAndRight = '└'
  private val upAndLeft = '┘'
  private val aliveCell = '☺'
  private val deadCell = ' '
  private val lineSeparator = '\n'

  private val generationFormats = {
    Stream(
      s"$horizontal Generation: %s $horizontal",
      " Generation: %s ",
      "Generation: %s",
      s"$horizontal Gen: %s $horizontal",
      " Gen: %s ",
      "Gen: %s",
      "G: %s",
      " %s ",
      "%s",
      ""
    )
  }

  def apply(game: Game)(board: game.Board): String = {
    import game._

    val allRows = 0 until rows
    val allColumns = 0 until columns
    val stringLength = (rows + 2) * (columns + 3)
    val stringBuilder = new StringBuilder(stringLength)

    import board._

    stringBuilder append lineSeparator append downAndRight
    allColumns foreach (_ => stringBuilder append horizontal)
    stringBuilder append downAndLeft append lineSeparator
    for (row <- allRows) {
      stringBuilder append '│'
      for (column <- allColumns) {
        stringBuilder append (if (alive(Position(row, column))) aliveCell else deadCell)
      }
      stringBuilder append vertical append lineSeparator
    }
    stringBuilder append upAndRight
    generationFormats
      .map(_ format generation)
      .find(_.length <= columns)
      .foreach { generationString =>
        stringBuilder append generationString
        0 until columns - generationString.length foreach (_ => stringBuilder append horizontal)
      }
    stringBuilder append upAndLeft
    stringBuilder.toString ensuring (_.length == stringLength)
  }
}
