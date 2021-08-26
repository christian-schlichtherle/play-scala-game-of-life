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
    LazyList(
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

    val allRows = 0.until(rows)
    val allColumns = 0.until(columns)
    val length = (rows + 2) * (columns + 3)
    val builder = new StringBuilder(length)

    import board._
    import builder._

    append(lineSeparator).append(downAndRight)
    allColumns.foreach(_ => append(horizontal))
    append(downAndLeft).append(lineSeparator)
    for (row <- allRows) {
      append(vertical)
      for (column <- allColumns) {
        append(if (alive(Position(row, column))) aliveCell else deadCell)
      }
      append(vertical).append(lineSeparator)
    }
    append(upAndRight)
    generationFormats
      .map(_.format(generation))
      .find(_.length <= columns)
      .foreach { generationString =>
        append(generationString)
        0.until(columns - generationString.length).foreach(_ => append(horizontal))
      }
    append(upAndLeft)
    builder.toString.ensuring(_.length == length)
  }
}
