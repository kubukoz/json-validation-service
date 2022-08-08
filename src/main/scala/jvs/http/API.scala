package jvs.http

import cats.Applicative
import cats.effect.Concurrent
import io.circe.Json
import jvs.SchemaId
import org.http4s.Method._
import org.http4s.Uri
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import jvs.ActionResult
import jvs.ActionStatus

trait API[F[_]] {
  def uploadSchema(schemaId: SchemaId, schema: Json): F[ActionResult]
}

object API {

  def apply[F[_]](implicit F: API[F]): API[F] = F

  def server[F[_]: Applicative]: API[F] =
    new API[F] {

      def uploadSchema(schemaId: SchemaId, schema: Json): F[ActionResult] = Applicative[F].pure(
        ActionResult.UploadSchema(schemaId, ActionStatus.Success)
      )

    }

  def client[F[_]: Concurrent](client: Client[F], baseUrl: Uri): API[F] =
    new API[F] {
      private val dsl = new Http4sClientDsl[F] {}
      import dsl._

      def uploadSchema(
        schemaId: SchemaId,
        schema: Json,
      ): F[ActionResult] = client.expect(
        POST.apply(baseUrl / "schema" / schemaId.value).withEntity(schema)
      )

    }

}
