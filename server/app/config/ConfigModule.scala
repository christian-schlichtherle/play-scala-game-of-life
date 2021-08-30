package config

import bali.Lookup
import play.api.Configuration

trait ConfigModule {

  @Lookup
  def configuration: Configuration // builtin

  final lazy val config: GameConfig = GameConfig(configuration)
}
