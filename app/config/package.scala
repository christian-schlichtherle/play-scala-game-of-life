import models.Game.Setup
import play.api.Configuration

import scala.reflect.runtime.currentMirror
import scala.tools.reflect.ToolBox
import scala.util.Random

package object config {

  final case class GameConfig(fps: Int, secs: Int, setupCode: String) {

    lazy val setup: Setup = {
      setupCode match {
        case "random" => (_, _) => Random.nextBoolean()
        case other => evaluate("($r: Int, $c: Int) => { " + other + " }: Boolean")
      }
    }

    private def evaluate[A](string: String): A = {
      val tb = currentMirror.mkToolBox()
      tb.eval(tb.parse(string)).asInstanceOf[A]
    }
  }

  object GameConfig {

    def apply(c: Configuration): GameConfig = {
      val game = c.get[Configuration]("game")
      import game._
      GameConfig(
        fps = get[Int]("fps"),
        secs = get[Int]("secs"),
        setupCode = get[String]("setup"),
      )
    }
  }
}
