package jvs.http

import cats.Applicative
import cats.effect.Concurrent
import io.circe.Json
import org.http4s.EntityDecoder
import org.http4s.Method._
import org.http4s.Uri
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl

import java.util.UUID

trait API[F[_]] {
  def uploadSchema(schemaId: UUID, schema: Json): F[Unit]
}

object API {

  def apply[F[_]](implicit F: API[F]): API[F] = F

  def server[F[_]: Applicative]: API[F] =
    new API[F] {
      def uploadSchema(schemaId: UUID, schema: Json): F[Unit] = Applicative[F].unit
    }

  def client[F[_]: Concurrent](client: Client[F], baseUrl: Uri): API[F] =
    new API[F] {
      private val dsl = new Http4sClientDsl[F] {}
      import dsl._

      def uploadSchema(
        schemaId: UUID,
        schema: Json,
      ): F[Unit] =
        client
          .expect(PUT.apply(baseUrl / "schema" / schemaId).withEntity(schema))(
            // to avoid trying to decode "unit" from a Json object
            EntityDecoder.void[F]
          )

    }

}
