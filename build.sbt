lazy val root = project.in(file(".")).enablePlugins(PlayScala)

libraryDependencies ++= Seq(
  "global.namespace.bali" %% "bali-scala" % "0.5.3" % Provided,
  "org.scala-lang" % "scala-compiler" % scalaVersion.value,
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
)

name := "play-scala-game-of-life"

scalacOptions := Seq("-deprecation", "-feature")

scalaVersion := "2.13.6"

version := "0.1.1-SNAPSHOT"
