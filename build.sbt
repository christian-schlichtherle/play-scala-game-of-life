inThisBuild(Seq(
  libraryDependencies ++= Seq(
    "global.namespace.bali" %% "bali-scala" % "0.5.3" % Provided,
  ),
  scalaVersion := "2.13.6",
  scalacOptions := Seq("-deprecation", "-feature"),
  version := "0.2.0-SNAPSHOT",
))

lazy val client = project
  .enablePlugins(ScalaJSPlugin, ScalaJSWeb)
  .dependsOn(shared.js)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "1.2.0",
  )

lazy val root = project
  .in(file("."))
  .aggregate(client, server, shared.js, shared.jvm)
  .settings(
    name := "play-scala-game-of-life",
  )

lazy val server = project
  .enablePlugins(PlayScala)
  .dependsOn(shared.jvm)
  .settings(
    pipelineStages := Seq(digest, gzip),
    libraryDependencies ++= Seq(
      "com.vmunier" %% "scalajs-scripts" % "1.2.0",
      "org.scala-lang" % "scala-compiler" % scalaVersion.value,
    ),
    scalaJSProjects := Seq(client),

    Assets / pipelineStages  := Seq(scalaJSPipeline),

    // triggers scalaJSPipeline when using compile or continuous compilation
    Compile / compile := ((Compile / compile) dependsOn scalaJSPipeline).value,
  )

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))
  .jsConfigure(_.enablePlugins(ScalaJSWeb))
