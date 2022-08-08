package jvs.persistence

import cats.effect._
import cats.implicits._
import fs2.io.net.Network
import natchez.Trace.Implicits.noop
import skunk.Session
import skunk.util.Pool

object SkunkClient {

  def connectionPool[F[_]: Network: std.Console: Concurrent](
    config: DatabaseConfig
  ): Pool[F, Session[F]] = Session.pooled[F](
    config.host.toString,
    config.port.value,
    config.user,
    config.database,
    config.password.some,
    max = 10,
  )

}
