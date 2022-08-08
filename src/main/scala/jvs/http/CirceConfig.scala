package jvs.http

import io.circe.generic.extras.Configuration

object CirceConfig {

  private def uncapitalize(s: String) =
    if (s.isEmpty())
      s
    else
      s.head.toLower.toString + s.tail

  val base: Configuration = Configuration
    .default
    .copy(transformConstructorNames = uncapitalize)

}
