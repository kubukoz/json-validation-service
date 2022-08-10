Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion := "2.13.8"
ThisBuild / githubWorkflowPublishTargetBranches := List(
  RefPredicate.Equals(Ref.Branch("deploy"))
)
ThisBuild / githubWorkflowBuild := List(WorkflowStep.Sbt(List("ci")))
ThisBuild / githubWorkflowPublish := List(WorkflowStep.Sbt(List("deploy")))
ThisBuild / githubWorkflowGeneratedCI ~= {
  _.map {
    case job if job.id == "publish" =>
      job
        .copy(
          env =
            job.env ++ Map(
              "E2E_BASE_URL" -> "https://json-validation.herokuapp.com",
              "HEROKU_API_KEY" -> s"$${{ secrets.HEROKU_API_KEY }}",
            )
        )
    case job => job
  }
}

val commonSettings = Seq(
  organization := "com.kubukoz.jvs",
  scalacOptions -= "-Xfatal-warnings",
  scalacOptions += "-Xsource:3.0",
  testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
  IntegrationTest / testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
  libraryDependencies ++= Seq(
    "is.cir" %% "ciris" % "2.3.3",
    "ch.qos.logback" % "logback-classic" % "1.2.11",
    "io.circe" %% "circe-parser" % "0.14.2",
    "org.typelevel" %% "log4cats-noop" % "2.4.0" % "it,test",
    "io.circe" %% "circe-literal" % "0.14.2" % "it,test",
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
  .configs(E2EConfig, IntegrationTest)
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
      "org.tpolecat" %% "skunk-core" % "0.2.3",
      "org.tpolecat" %% "skunk-circe" % "0.2.3",
      "com.github.java-json-tools" % "json-schema-validator" % "2.2.14",
      "com.disneystreaming" %% "weaver-cats" % "0.7.14" % "it,test",
      "org.http4s" %% "http4s-ember-client" % "0.23.14" % "it",
    ),
    addCommandAlias(
      "ci",
      List("test", "Docker/publishLocal", "composeUp", "IntegrationTest/test", "e2e/E2EConfig/test")
        .mkString(";"),
    ),
    addCommandAlias("deploy", List("stage", "deployHeroku", "e2e/E2EConfig/test").mkString(";")),
  )
  .settings(
    Compile / herokuAppName := "json-validation"
  )
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .aggregate(e2e)
