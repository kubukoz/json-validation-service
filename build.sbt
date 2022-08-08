Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion := "2.13.8"
ThisBuild / githubWorkflowPublishTargetBranches := Nil

ThisBuild / githubWorkflowAddedJobs ++= Seq(
  WorkflowJob(
    id = "e2e",
    name = "E2E Tests",
    steps =
      githubWorkflowJobSetup.value.toList ++ List(
        WorkflowStep.Sbt(commands = List("Docker/publishLocal")),
        WorkflowStep.Run(
          commands = List("docker-compose up -d", "sbt e2e/test")
        ),
        WorkflowStep.Run(
          commands = List("docker-compose down"),
          cond = Some("always()"),
        ),
      ),
  )
)

val commonSettings = Seq(
  organization := "com.kubukoz.jvs",
  scalacOptions -= "-Xfatal-warnings",
  scalacOptions += "-Xsource:3.0",
  testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
  libraryDependencies ++= Seq(
    "is.cir" %% "ciris" % "2.3.3",
    "ch.qos.logback" % "logback-classic" % "1.2.11",
    compilerPlugin("org.polyvariant" % "better-tostring" % "0.3.16" cross CrossVersion.full),
  ),
)

lazy val e2e = project
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-client" % "0.23.14",
      "com.disneystreaming" %% "weaver-cats" % "0.7.14" % Test,
    ),
  )

val root = project
  .in(file("."))
  .settings(
    name := "json-validation-service",
    dockerUpdateLatest := true,
    commonSettings,
    libraryDependencies ++= Seq(
      "com.disneystreaming" %% "weaver-cats" % "0.7.14" % Test,
      "org.http4s" %% "http4s-circe" % "0.23.14",
      "org.http4s" %% "http4s-dsl" % "0.23.14",
      "org.http4s" %% "http4s-ember-server" % "0.23.14",
    ),
  )
  .enablePlugins(JavaAppPackaging, DockerPlugin)
