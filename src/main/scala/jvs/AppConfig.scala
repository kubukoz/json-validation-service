package jvs

import ciris.ConfigValue
import jvs.http.HttpConfig

object AppConfig {
  def config[F[_]]: ConfigValue[F, AppConfig] = HttpConfig.config[F].map(apply)
}

final case class AppConfig(http: HttpConfig)
