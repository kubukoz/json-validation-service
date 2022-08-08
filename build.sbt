Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion := "2.13.8"
ThisBuild / githubWorkflowPublishTargetBranches := Nil
ThisBuild / githubWorkflowBuild := List(WorkflowStep.Sbt(List("ci")))

val commonSettings = Seq(
  organization := "com.kubukoz.jvs",
  scalacOptions -= "-Xfatal-warnings",
  scalacOptions += "-Xsource:3.0",
  testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
  libraryDependencies ++= Seq(
    "is.cir" %% "ciris" % "2.3.3",
    "ch.qos.logback" % "logback-classic" % "1.2.11",
    "io.circe" %% "circe-parser" % "0.14.2",
    "org.typelevel" %% "log4cats-noop" % "2.4.0" % Test,
    "io.circe" %% "circe-literal" % "0.14.2" % Test,
    compilerPlugin("org.polyvariant" % "better-tostring" % "0.3.16" cross CrossVersion.full),
    compilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full),
  ),
)

val E2EConfig = config("e2e").extend(Test)

lazy val e2e = project
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-client" % "0.23.14" % Test,
      "com.disneystreaming" %% "weaver-cats" % "0.7.14" % Test,
      "org.http4s" %% "http4s-circe" % "0.23.14" % Test,
    ),
  )
  .configs(E2EConfig)
  .settings(
    inConfig(E2EConfig)(
      Defaults.testSettings ++ bloop.integrations.sbt.BloopDefaults.configSettings
    )
  )

val root = project
  .in(file("."))
  .settings(
    name := "json-validation-service",
    dockerUpdateLatest := true,
    commonSettings,
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-circe" % "0.23.14",
      "org.http4s" %% "http4s-dsl" % "0.23.14",
      "org.http4s" %% "http4s-ember-server" % "0.23.14",
      "org.http4s" %% "http4s-client" % "0.23.14",
      "io.circe" %% "circe-generic-extras" % "0.14.2",
      "com.disneystreaming" %% "weaver-cats" % "0.7.14" % Test,
    ),
    addCommandAlias(
      "ci",
      List("test", "Docker/publishLocal", "composeUp", "e2e/E2EConfig/test").mkString(";"),
    ),
  )
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .aggregate(e2e)
