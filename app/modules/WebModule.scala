package modules

import models.Game
import play.api._
import play.api.inject._

import javax.inject.Provider

class WebModule extends Module {

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    val config = configuration.get[Configuration]("game")
    val provider: Provider[Game] = () => Game(config)
    Seq(
      bind[Configuration].qualifiedWith("game").to(config),
      bind[Game].to(provider),
    )
  }
}
