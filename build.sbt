lazy val root = project
  .in(file("."))
  .enablePlugins(PlayScala)
  .settings(
    fork := true, // required to make Ctl-C work and respect `javaOptions`
    javaOptions := Seq("-ea"),
    libraryDependencies ++= Seq(
      filters,
      guice,
      "org.scala-lang" % "scala-compiler" % scalaVersion.value,
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
      "org.webjars.npm" % "google-closure-library" % "20170124.0.0",
    ),
    name := "play-scala-game-of-life",
    scalacOptions := Seq("-deprecation", "-feature"),
    scalaVersion := "2.13.6",
    version := "0.1.1-SNAPSHOT",

    // http://www.scalatest.org/user_guide/using_scalatest_with_sbt
    Test / logBuffered := false,
    Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oD"),
  )
