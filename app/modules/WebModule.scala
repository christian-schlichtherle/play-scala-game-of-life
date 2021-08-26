package modules

import models.Game
import play.api.inject._
import play.api._

class WebModule extends Module {

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    Seq(
      bind[Game].to(Game(configuration.underlying.getConfig("game")))
    )
  }
}
