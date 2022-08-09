package jvs.http

import cats.effect.Concurrent
import cats.effect.Resource
import cats.effect.kernel.Async
import cats.implicits._
import jvs.http.HttpConfig
import jvs.model.SchemaId
import org.http4s.HttpApp
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec._
import org.http4s.circe.middleware.JsonDebugErrorHandler
import org.http4s.dsl.Http4sDsl
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server

import util.chaining._
import io.circe.Json

object HttpServer {

  def run[F[_]: Async](route: HttpApp[F], config: HttpConfig): Resource[F, Server] =
    EmberServerBuilder
      .default[F]
      .withPort(config.port)
      .withHost(config.host)
      .withHttpApp(route)
      .build

  def routes[F[_]: Concurrent](api: API[F]): HttpApp[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes
      .of[F] {
        case req @ (POST -> Root / "schema" / schemaId) =>
          req
            .bodyText
            .compile
            .string
            .flatMap { schema =>
              api.uploadSchema(SchemaId(schemaId), schema)
            }
            .flatMap {
              case result if result.isSuccess => Created(result)
              case result                     => UnprocessableEntity(result)
            }

        case GET -> Root / "schema" / schemaId =>
          api
            .downloadSchema(SchemaId(schemaId))
            .flatMap {
              case Left(e)       => NotFound(e)
              case Right(schema) => Ok(schema)
            }

        case req @ POST -> Root / "validate" / schemaId =>
          req
            .decode[Json] { body =>
              api
                .validateDocument(SchemaId(schemaId), body)
                .flatMap {
                  case result if result.isSuccess => Ok(result)
                  case result                     => UnprocessableEntity(result)
                }
            }

      }
      .orNotFound
      .pipe(JsonDebugErrorHandler[F, F](_))
  }

}
