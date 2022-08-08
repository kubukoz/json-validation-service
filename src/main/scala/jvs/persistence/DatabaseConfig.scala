package jvs.persistence

import cats.implicits._
import ciris.ConfigValue
import com.comcast.ip4s._

import jvs.config.ConfigDecoders._

final case class DatabaseConfig(
  host: Host,
  port: Port,
  user: String,
  database: String,
  password: String,
  maxConnections: Int,
)

object DatabaseConfig {

  def config[F[_]]: ConfigValue[F, DatabaseConfig] = {
    import ciris._

    (
      env("DB_HOST").as[Host].default(host"localhost"),
      env("DB_PORT").as[Port].default(port"5432"),
      env("DB_USER").as[String].default("postgres"),
      env("DB_DATABASE").as[String].default("postgres"),
      env("DB_PASSWORD").as[String].default("example"),
      env("DB_MAX_CONNECTIONS").as[Int].default(10),
    ).parMapN(apply)
  }

}
