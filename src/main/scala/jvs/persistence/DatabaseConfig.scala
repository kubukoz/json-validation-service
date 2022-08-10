package jvs.persistence

import cats.implicits._
import ciris.ConfigValue
import com.comcast.ip4s._

import jvs.config.ConfigDecoders._

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
