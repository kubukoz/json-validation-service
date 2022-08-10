package jvs.persistence

import cats.effect._
import cats.implicits._
import fs2.io.net.Network
import natchez.Trace.Implicits.noop
import skunk.Session
import skunk.util.Pool
import skunk.SSL

object SkunkClient {

  def connectionPool[F[_]: Network: std.Console: Concurrent](
    config: DatabaseConfig
  ): Pool[F, Session[F]] = Session.pooled[F](
    host = config.host.toString,
    port = config.port.value,
    user = config.user,
    database = config.database,
    password = config.password.some,
    max = config.maxConnections,
    ssl = SSL.System,
  )

}
