package modules

import com.google.inject.{AbstractModule, Provides}
import com.typesafe.config.Config
import models.Game
import play.api.Configuration

class WebModule extends AbstractModule {

  def configure(): Unit = { }

  @Provides
  def game(config: Config): Game = Game(config getConfig "game")

  @Provides
  def config(configuration: Configuration): Config = configuration.underlying
}
