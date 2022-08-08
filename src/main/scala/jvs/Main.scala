package jvs

import cats.effect.IO
import cats.effect.Resource
import cats.effect.ResourceApp
import cats.effect.implicits._
import cats.implicits._
import jvs.http.API
import jvs.http.HttpServer
import jvs.persistence.SchemaRepository
import jvs.services.SchemaService
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends ResourceApp.Forever {
  implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  def run(args: List[String]): Resource[IO, Unit] =
    AppConfig
      .config[IO]
      .resource
      .flatMap { appConfig =>
        SchemaRepository.inMemory[IO].toResource.flatMap { implicit schemaRepository =>
          implicit val schemaService: SchemaService[IO] = SchemaService.instance[IO]

          HttpServer.run(HttpServer.routes[IO](API.server), appConfig.http)
        }
      }
      .void

}
