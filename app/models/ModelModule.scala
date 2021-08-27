package models

import bali.Lookup
import play.api.Configuration

trait ModelModule {

  @Lookup("configuration") // builtin
  val config: Configuration

  final def game(config: Configuration): Game = Game(config)

  final lazy val gameConfig: Configuration = config.get[Configuration]("game")
}
