package client

import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom._

import scala.scalajs.js

object Main {

  private def querySelector(selectors: String): Element = document.querySelector(selectors)

  private object elem {

    val Seq(board, cell, stats) = {
      Seq("board", "cell", "stats").map(name => querySelector("#" + name).asInstanceOf[HTMLElement])
    }
  }

  private def metaContent(name: String) = {
    querySelector(s"""meta[name="$name"]""").getAttribute("content")
  }

  private object config {

    val Seq(fps, secs) = Seq("fps", "secs").map(metaContent).map(_.toInt)
  }

  private object board {

    def update(e: MessageEvent): Unit = {
      elem.board.innerHTML = {
        e.data.toString.foldLeft("")((s, c) => s + (if (c.isUpper) s"""<i class="$c"></i>""" else c))
      }
    }
  }

  private object stats {

    private var timing: Array[Double] = _
    private var generation, index = 0

    reset()

    def update(): Unit = {
      val now = window.performance.now()
      timing(index) = now
      index += 1
      index %= config.fps
      generation += 1
      elem.stats.innerHTML = if (config.fps <= generation) {
        val length = Math.min(config.fps, generation)
        val zen = timing(index % length)
        val rate = 1e3 * length / (now - zen)
        f"Generation: $generation<br>Generations / Second: $rate%1.1f"
      } else {
        s"Generation: $generation<br>&nbsp;"
      }
      elem.stats.setAttribute("style",
        "left:" + Math.abs((generation.toFloat / config.fps + 80) % 160 - 80) + '%')
    }

    def reset(): Unit = {
      timing = new Array[Double](config.fps)
      generation = 0
      index = 0
    }
  }

  private object source {

    private var es: EventSource = _

    def reset(cols: Int, rows: Int): Unit = {
      if (null ne es) {
        es.close()
        stats.reset()
      }
      es = new EventSource(s"/stream?cols=$cols&rows=$rows&secs=${config.secs}")
      es.addEventListener("message", board.update)
      es.addEventListener("message", (_: Event) => stats.update())
      es.addEventListener("close", (_: Event) => stats.reset())
    }
  }

  private object onResize {

    private val width = elem.cell.offsetWidth.toInt
    private val height = elem.board.offsetHeight.toInt

    private var pendingUpdate: Boolean = false

    def apply(): Unit = {
      if (!pendingUpdate) {
        pendingUpdate = true

        window.requestAnimationFrame(_ => {
          pendingUpdate = false

          val cols = visualViewport.width / width
          val rows = visualViewport.height / height

          source.reset(cols, rows)
        })
      }
    }
  }

  def main(args: Array[String]): Unit = {
    visualViewport.addEventListener("resize", (_: Event) => onResize())
    onResize()
  }

  private lazy val visualViewport = {
    window.asInstanceOf[js.Dynamic].visualViewport.asInstanceOf[IncompleteVisualViewport]
  }

  private trait IncompleteVisualViewport extends EventTarget {

    val height: Int
    val width: Int
  }
}
