Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion := "3.3.0"
ThisBuild / githubWorkflowPublishTargetBranches := List(
  RefPredicate.Equals(Ref.Branch("main"))
)
ThisBuild / githubWorkflowBuild := List(WorkflowStep.Sbt(List("ci")))
ThisBuild / githubWorkflowPublish := List(WorkflowStep.Sbt(List("deploy")))
// ThisBuild / githubWorkflowGeneratedCI := (ThisBuild / githubWorkflowGeneratedCI).value.map {
//   case job if job.id == "publish" =>
//     job
//       .copy(
//         env =
//           job.env ++ Map(
//             "E2E_BASE_URL" -> s"https://${(Compile / herokuAppName).value}.herokuapp.com",
//             "HEROKU_API_KEY" -> s"$${{ secrets.HEROKU_API_KEY }}",
//           )
//       )
//   case job => job
// }

val commonSettings = Seq(
  organization := "com.kubukoz.jvs",
  scalacOptions -= "-Xfatal-warnings",
  scalacOptions += "-Xsource:3.0",
  scalacOptions ++= Seq("-release", "8"),
  libraryDependencies ++= Seq(
    "is.cir" %% "ciris" % "3.2.0",
    "ch.qos.logback" % "logback-classic" % "1.2.11",
    "io.circe" %% "circe-parser" % "0.14.5",
    "org.typelevel" %% "log4cats-noop" % "2.6.0" % "it,test",
    "io.circe" %% "circe-literal" % "0.14.5" % "it,test",
    compilerPlugin("org.polyvariant" % "better-tostring" % "0.3.17" cross CrossVersion.full),
  ),
)

val E2EConfig = config("e2e").extend(Test)

lazy val e2e = project
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-client" % "0.23.22" % Test,
      "com.disneystreaming" %% "weaver-cats" % "0.8.3" % Test,
      "org.http4s" %% "http4s-circe" % "0.23.22" % Test,
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
      "org.http4s" %% "http4s-circe" % "0.23.22",
      "org.http4s" %% "http4s-dsl" % "0.23.22",
      "org.http4s" %% "http4s-ember-server" % "0.23.22",
      "org.http4s" %% "http4s-client" % "0.23.22",
      "org.tpolecat" %% "skunk-core" % "0.6.0",
      "org.tpolecat" %% "skunk-circe" % "0.6.0",
      "com.github.java-json-tools" % "json-schema-validator" % "2.2.14",
      "com.disneystreaming" %% "weaver-cats" % "0.8.3" % "it,test",
      "org.http4s" %% "http4s-ember-client" % "0.23.22" % "it",
    ),
    addCommandAlias(
      "ci",
      List("test", "Docker/publishLocal", "composeUp", "IntegrationTest/test", "e2e/E2EConfig/test")
        .mkString(";"),
    ),
    addCommandAlias("deploy", List("stage", "deployHeroku", "e2e/E2EConfig/test").mkString(";")),
  )
  // .settings(
  //   Compile / herokuAppName := "json-validation"
  // )
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .aggregate(e2e)
