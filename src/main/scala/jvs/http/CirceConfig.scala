package jvs.http

import io.circe.generic.extras.Configuration

object CirceConfig {
  implicit val default: Configuration = Configuration.default
}
