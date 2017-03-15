package models

import models.ConsoleGame._

import scala.collection.mutable

case class ConsoleGame(rows: Int, columns: Int) extends Game {

  require(rows >= 2)
  require(columns >= 2)

  /** Returns a stateful function which renders any given board as a string. */
  def render: Board => String = {
    val allRows = 0 until rows
    val allColumns = 0 until columns
    val stringLength = (rows + 2) * (columns + 3)
    val stringBuilder = new mutable.StringBuilder(stringLength)

    { board =>
      import board._

      stringBuilder clear ()
      stringBuilder append "\n+"
      allColumns foreach (_ => stringBuilder append '-')
      stringBuilder append "+\n"
      for (row <- allRows) {
        stringBuilder append '+'
        for (column <- allColumns) {
          stringBuilder append (if (alive(Position(row, column))) 'O' else ' ')
        }
        stringBuilder append "+\n"
      }
      stringBuilder append '+'
      generationFormats
        .map(_ format generation)
        .find(_.length <= columns)
        .foreach { generationString =>
          stringBuilder append generationString
          0 until columns - generationString.length foreach (_ => stringBuilder append '-')
        }
      stringBuilder append '+'
      stringBuilder.toString ensuring (_.length == stringLength)
    }
  }
}

object ConsoleGame {

  private val generationFormats = {
    Stream(
      "- Generation: %s -",
      " Generation: %s ",
      "Generation: %s",
      "- Gen: %s -",
      " Gen: %s ",
      "Gen: %s",
      "G: %s",
      " %s ",
      "%s",
      ""
    )
  }
}
