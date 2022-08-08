package jvs.http

import cats.Applicative
import cats.effect.Concurrent
import cats.implicits._
import jvs.model.SchemaId
import jvs.transport.ActionResult
import org.http4s.Method._
import org.http4s.Uri
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.typelevel.log4cats.Logger

trait API[F[_]] {
  def uploadSchema(schemaId: SchemaId, schema: String): F[ActionResult]
}

object API {

  def apply[F[_]](implicit F: API[F]): API[F] = F

  def server[F[_]: Applicative: Logger]: API[F] =
    new API[F] {

      def uploadSchema(schemaId: SchemaId, schema: String): F[ActionResult] =
        io.circe.parser.parse(schema) match {
          case Left(e) =>
            Logger[F]
              .debug(e)("Failed to parse schema")
              .as(
                ActionResult.uploadSchemaError(
                  schemaId,
                  "Invalid JSON",
                )
              )
          case Right(_) =>
            Applicative[F].pure(
              ActionResult.uploadSchemaSuccess(schemaId)
            )
        }

    }

  def client[F[_]: Concurrent](client: Client[F], baseUrl: Uri): API[F] =
    new API[F] {
      private val dsl = new Http4sClientDsl[F] {}
      import dsl._

      def uploadSchema(
        schemaId: SchemaId,
        schema: String,
      ): F[ActionResult] = client.expect(
        POST(baseUrl / "schema" / schemaId.value).withEntity(schema)
      )

    }

}
