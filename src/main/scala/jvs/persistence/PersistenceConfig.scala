package jvs.persistence

import cats.implicits._
import ciris.ConfigValue
import com.comcast.ip4s._

import jvs.config.ConfigDecoders._
import skunk.SSL

sealed trait PersistenceConfig extends Product with Serializable

object PersistenceConfig {
  final case class WithDatabase(db: DatabaseConfig) extends PersistenceConfig
  case object InMemory extends PersistenceConfig

  def config[F[_]]: ConfigValue[F, PersistenceConfig] = {
    import ciris._

    val inMemory = env("DB_IN_MEMORY").map(_ => InMemory).widen[PersistenceConfig]

    val withDatabase = DatabaseConfig.config[F].map(WithDatabase(_)).widen[PersistenceConfig]

    inMemory.or(withDatabase)
  }

}

final case class DatabaseConfig(
  host: Host,
  port: Port,
  user: String,
  database: String,
  password: String,
  maxConnections: Int,
  ssl: SSL,
)

object DatabaseConfig {

  def config[F[_]]: ConfigValue[F, DatabaseConfig] = {
    import ciris._

    val ssl = env("DB_USE_SSL")
      .as[Boolean]
      .default(false)
      .map {
        case true =>
          // Hack: this trusts all certificates.
          // Doing this properly (adding the EC2 certificate to the trust store)
          // was considered too time-consuming for the purposes of this task.
          SSL.Trusted

        case false => SSL.None
      }

    (
      env("POSTGRES_HOST").as[Host].default(host"localhost"),
      env("POSTGRES_PORT").as[Port].default(port"5432"),
      env("POSTGRES_USERNAME").as[String].default("postgres"),
      env("POSTGRES_PASSWORD").as[String].default("example"),
      env("POSTGRES_DATABASE").as[String].default("postgres"),
      env("DB_MAX_CONNECTIONS").as[Int].default(10),
      ssl,
    ).parMapN(apply)
  }

}
