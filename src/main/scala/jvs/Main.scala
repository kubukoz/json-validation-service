package jvs

import cats.effect.IO
import cats.effect.Resource
import cats.effect.ResourceApp
import cats.implicits._
import jvs.http.API
import jvs.http.HttpServer
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.SelfAwareStructuredLogger
import jvs.services.SchemaService

object Main extends ResourceApp.Forever {
  implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  def run(args: List[String]): Resource[IO, Unit] =
    AppConfig
      .config[IO]
      .resource
      .flatMap { appConfig =>
        implicit val schemaService: SchemaService[IO] = SchemaService.instance[IO]

        HttpServer.run(HttpServer.routes[IO](API.server), appConfig.http)
      }
      .void

}
