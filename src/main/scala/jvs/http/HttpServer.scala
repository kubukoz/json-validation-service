package jvs.http

import cats.Monad
import cats.effect.Resource
import cats.effect.kernel.Async
import jvs.http.HttpConfig
import org.http4s.HttpApp
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server

object HttpServer {

  def run[F[_]: Async](route: HttpApp[F], config: HttpConfig): Resource[F, Server] =
    EmberServerBuilder
      .default[F]
      .withPort(config.port)
      .withHost(config.host)
      .withHttpApp(route)
      .build

  def routes[F[_]: Monad]: HttpApp[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] { case GET -> Root / "hello" => Ok("Hello World!") }.orNotFound
  }

}
