package jvs.http

import cats.Applicative
import cats.effect.Concurrent
import jvs.model.SchemaId
import jvs.transport.ActionKind
import jvs.transport.ActionResult
import jvs.transport.ActionStatus
import org.http4s.Method._
import org.http4s.Uri
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl

trait API[F[_]] {
  def uploadSchema(schemaId: SchemaId, schema: String): F[ActionResult]
}

object API {

  def apply[F[_]](implicit F: API[F]): API[F] = F

  def server[F[_]: Applicative]: API[F] =
    new API[F] {

      def uploadSchema(schemaId: SchemaId, schema: String): F[ActionResult] = Applicative[F].pure(
        ActionResult(
          action = ActionKind.UploadSchema,
          schemaId,
          status = ActionStatus.Success,
          message = None,
        )
      )

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
