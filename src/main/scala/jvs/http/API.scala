package jvs.http

import cats.MonadThrow
import cats.effect.Concurrent
import cats.implicits._
import jvs.model.Schema
import jvs.model.SchemaId
import jvs.services.AppError
import jvs.services.SchemaService
import jvs.transport.ActionResult
import org.http4s.Method._
import org.http4s.Request
import org.http4s.Uri
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.typelevel.log4cats.Logger
import io.circe.Json
import org.http4s.Status
import cats.Applicative
import org.http4s.client.UnexpectedStatus

trait API[F[_]] {
  def uploadSchema(schemaId: SchemaId, schema: String): F[ActionResult]
  def downloadSchema(schemaId: SchemaId): F[Option[Json]]
  def validateDocument(schemaId: SchemaId, document: Json): F[ActionResult]
}

object API {

  def apply[F[_]](implicit F: API[F]): API[F] = F

  def server[F[_]: SchemaService: MonadThrow: Logger]: API[F] =
    new API[F] {

      def uploadSchema(schemaId: SchemaId, schema: String): F[ActionResult] = {
        def invalidJson(e: Throwable): F[ActionResult] = Logger[F]
          .debug(e)("Failed to parse schema")
          .as(
            ActionResult.uploadSchemaError(
              schemaId,
              "Invalid JSON",
            )
          )

        val actionResult =
          io.circe.parser.parse(schema) match {
            case Left(e) => invalidJson(e)
            case Right(schemaJSON) =>
              SchemaService[F]
                .persistSchema(Schema(schemaId, schemaJSON))
                .as(ActionResult.uploadSchemaSuccess(schemaId))
          }

        actionResult
          .recover { case e: AppError => ActionResult.uploadSchemaError(schemaId, e.getMessage) }
      }

      def downloadSchema(
        schemaId: SchemaId
      ): F[Option[Json]] = SchemaService[F]
        .getSchema(schemaId)
        .map(_.json.some)
        .recover { case AppError.SchemaNotFound => None }

      def validateDocument(
        schemaId: SchemaId,
        document: Json,
      ): F[ActionResult] = SchemaService[F]
        .validateDocument(schemaId, document)
        .as(ActionResult.validateDocumentSuccess(schemaId))
        .recover { case e: AppError =>
          ActionResult.validateDocumentError(schemaId, e.getMessage)
        }

    }

  def client[F[_]: Concurrent](client: Client[F], baseUrl: Uri): API[F] = {
    val raw = clientRaw[F](baseUrl)

    new API[F] {

      def uploadSchema(
        schemaId: SchemaId,
        schema: String,
      ): F[ActionResult] = client
        .run(raw.uploadSchema(schemaId, schema))
        .use(_.as[ActionResult])

      def downloadSchema(schemaId: SchemaId): F[Option[Json]] = {
        val request = raw.downloadSchema(schemaId)

        client
          .run(request)
          .use {
            case response if response.status.isSuccess           => response.as[Json].map(_.some)
            case response if response.status === Status.NotFound => Applicative[F].pure(none)
            case response =>
              UnexpectedStatus(response.status, request.method, request.uri)
                .raiseError[F, Option[Json]]
          }
      }

      def validateDocument(schemaId: SchemaId, document: Json): F[ActionResult] = client
        .run(raw.validateDocument(schemaId, document))
        .use(_.as[ActionResult])

    }
  }

  type ToRequest[F[_], _] = Request[F]

  // API instance where all methods return a serialized form of the request.
  def clientRaw[F[_]](baseUrl: Uri): API[ToRequest[F, *]] =
    new API[ToRequest[F, *]] {
      private val dsl = new Http4sClientDsl[F] {}
      import dsl._

      def uploadSchema(
        schemaId: SchemaId,
        schema: String,
      ): Request[F] = POST(baseUrl / "schema" / schemaId.value).withEntity(schema)

      def downloadSchema(schemaId: SchemaId): Request[F] = GET(baseUrl / "schema" / schemaId.value)

      def validateDocument(schemaId: SchemaId, document: Json): Request[F] = POST(
        baseUrl / "validate" / schemaId.value
      ).withEntity(document)

    }

}
