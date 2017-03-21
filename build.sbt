lazy val root = (project in file(".")).enablePlugins(PlayScala)

fork in Test := true

javaOptions in Test := Seq("-ea")

libraryDependencies ++= Seq(
  filters,
  "org.webjars.npm" % "google-closure-library" % "20170124.0.0",
  //"org.mockito" % "mockito-core" % "2.7.9" % Test,
  "org.scala-lang" % "scala-compiler" % scalaVersion.value,
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test
)

name := "play-scala-game-of-life"

scalacOptions := Seq("-deprecation", "-feature", "-Xfuture")

scalaVersion := "2.11.8"

version := "0.1-SNAPSHOT"
