package jvs

import cats.implicits._
import ciris.ConfigValue
import jvs.http.HttpConfig
import jvs.persistence.DatabaseConfig

object AppConfig {

  def config[
    F[_]
  ]: ConfigValue[F, AppConfig] = (HttpConfig.config[F], DatabaseConfig.config[F]).parMapN(apply)

}

final case class AppConfig(http: HttpConfig, db: DatabaseConfig)
