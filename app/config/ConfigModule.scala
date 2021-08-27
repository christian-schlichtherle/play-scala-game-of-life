package config

import bali.Lookup
import play.api.Configuration

import java.util.Locale
import scala.reflect.runtime.currentMirror
import scala.tools.reflect.ToolBox
import scala.util.Random

trait ConfigModule {

  @Lookup
  private[config] val configuration: Configuration // builtin

  final lazy val config: GameConfig = {
    val underlying = configuration.get[Configuration]("game")
    import underlying._
    GameConfig(
      fps = get[Int]("fps"),
      secs = get[Int]("secs"),
      setup = {
        get[String]("setup").toLowerCase(Locale.ENGLISH) match {
          case "random" => (_, _) => Random.nextBoolean()
          case other => evaluate("($r: Int, $c: Int) => { " + other + " }: Boolean")
        }
      },
    )
  }

  private def evaluate[A](string: String): A = {
    val tb = currentMirror.mkToolBox()
    tb.eval(tb.parse(string)).asInstanceOf[A]
  }
}
