package jvs

import cats.effect.IO
import cats.effect.Resource
import cats.effect.ResourceApp
import cats.implicits._
import jvs.http.API
import jvs.http.HttpServer
import jvs.persistence.SchemaRepository
import jvs.persistence.SkunkClient
import jvs.services.SchemaService
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import jvs.persistence.PersistenceConfig
import cats.effect.implicits._

object Main extends ResourceApp.Forever {
  implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  def run(args: List[String]): Resource[IO, Unit] = AppConfig
    .config[IO]
    .resource
    .flatMap { appConfig =>
      val mkRepository =
        appConfig.persistence match {
          case PersistenceConfig.InMemory => SchemaRepository.inMemory[IO].toResource
          case PersistenceConfig.WithDatabase(db) =>
            SkunkClient.connectionPool[IO](db).evalMap(SchemaRepository.skunkBased(_))
        }

      mkRepository
        .flatMap { implicit schemaRepository =>
          implicit val schemaService: SchemaService[IO] = SchemaService.instance[IO]

          HttpServer.run(HttpServer.routes[IO](API.server), appConfig.http).void
        }
    }

}
