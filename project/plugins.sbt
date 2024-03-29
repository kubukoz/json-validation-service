libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)

addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.4.3")
addSbtPlugin("com.codecommit" % "sbt-github-actions" % "0.14.2")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.9")
addSbtPlugin("ch.epfl.scala" % "sbt-bloop" % "1.5.1")
addSbtPlugin("com.heroku" % "sbt-heroku" % "2.1.4")

addDependencyTreePlugin
