lazy val root = (project in file(".")).enablePlugins(PlayScala)

fork in Test := true

javaOptions in Test := Seq("-ea")

libraryDependencies ++= Seq(
  guice,
  "javax.xml.bind" % "jaxb-api" % "2.3.0" % Runtime,
  "org.webjars.npm" % "google-closure-library" % "20170124.0.0",
  "org.scala-lang" % "scala-compiler" % scalaVersion.value,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
)

name := "play-scala-game-of-life"

scalacOptions := Seq("-deprecation", "-feature", "-Xfuture")

scalaVersion := "2.11.12" // significantly faster than 2.12.5

version := "0.1.0-SNAPSHOT"
