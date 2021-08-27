import models.Game.Setup

package object config {

  final case class GameConfig(fps: Int, secs: Int, setup: Setup)
}
