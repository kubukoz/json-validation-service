package jvs

import cats.effect.IO
import cats.effect.Resource
import cats.effect.ResourceApp
import cats.implicits._
import jvs.http.HttpServer

object Main extends ResourceApp.Forever {

  def run(args: List[String]): Resource[IO, Unit] =
    AppConfig
      .config[IO]
      .resource
      .flatMap { appConfig =>
        HttpServer.run(HttpServer.routes[IO], appConfig.http)
      }
      .void

}
