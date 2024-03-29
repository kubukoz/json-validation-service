package jvs.persistence

import cats.effect._
import cats.implicits._
import fs2.io.net.Network
import natchez.Trace.Implicits.noop
import skunk.Session

object SkunkClient {

  def connectionPool[F[_]: Network: std.Console: Temporal](
    config: DatabaseConfig
  ): Resource[F, Resource[F, Session[F]]] = Session.pooled[F](
    host = config.host.toString,
    port = config.port.value,
    user = config.user,
    database = config.database,
    password = config.password.value.some,
    max = config.maxConnections,
    ssl = config.ssl,
  )

}
