package jvs.e2e

import org.http4s.Uri

import org.http4s.implicits._
import cats.implicits._
import ciris.ConfigValue

final case class E2EConfig(baseUrl: Uri)

object E2EConfig {

  def config[F[_]]: ConfigValue[F, E2EConfig] = {
    import ciris._

    implicit val decodeUri: ConfigDecoder[String, Uri] = ConfigDecoder.lift { s =>
      Uri.fromString(s).leftMap(e => ConfigError(e.message))
    }

    env("E2E_BASE_URL")
      .as[Uri]
      .default(uri"http://localhost:4000")
      .map(E2EConfig.apply)
  }

}
