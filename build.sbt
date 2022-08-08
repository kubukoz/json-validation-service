Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion := "2.13.8"
ThisBuild / githubWorkflowPublishTargetBranches := Nil

val root = project
  .in(file("."))
  .settings(
    scalacOptions -= "-Xfatal-warnings",
    scalacOptions += "-Xsource:3.0",
    libraryDependencies ++= Seq(
      compilerPlugin("org.polyvariant" % "better-tostring" % "0.3.16" cross CrossVersion.full)
    ),
  )
