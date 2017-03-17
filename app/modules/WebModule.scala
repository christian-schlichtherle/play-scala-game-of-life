package modules

import com.google.inject.{AbstractModule, Provides}
import com.typesafe.config.Config
import controllers.GameController
import play.api.Configuration

class WebModule extends AbstractModule {

  def configure(): Unit = { }

  @Provides
  def gameContext(config: Config): GameController.Context = GameController.Context(config getConfig "game")

  @Provides
  def config(configuration: Configuration): Config = configuration.underlying
}
