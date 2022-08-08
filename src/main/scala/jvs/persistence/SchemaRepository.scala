package jvs.persistence

import cats.Applicative
import cats.MonadThrow
import cats.effect.kernel.MonadCancelThrow
import cats.effect.kernel.Ref
import cats.effect.kernel.Resource
import cats.implicits._
import io.circe.Json
import jvs.model._
import jvs.persistence.PersistenceError
import skunk.Session
import skunk._
import skunk.implicits._
import skunk.exception.SkunkException

trait SchemaRepository[F[_]] {
  def insert(schema: Schema): F[Unit]
  def get(id: SchemaId): F[Schema]
}

object SchemaRepository {

  def apply[F[_]](implicit F: SchemaRepository[F]): SchemaRepository[F] = F

  def inMemory[
    F[_]: Ref.Make: MonadThrow
  ]: F[SchemaRepository[F]] = Ref[F].of(Map.empty[SchemaId, Json]).map { ref =>
    new SchemaRepository[F] {
      def insert(schema: Schema): F[Unit] =
        ref.modify {
          case map if map.contains(schema.id) =>
            (map, PersistenceError.Conflict.raiseError[F, Unit])

          case map => (map + (schema.id -> schema.json), Applicative[F].unit)
        }.flatten

      def get(
        id: SchemaId
      ): F[Schema] = ref
        .get
        .flatMap(_.get(id).liftTo[F](PersistenceError.NotFound))
        .map(Schema(id, _))

    }
  }

  def skunkBased[F[_]: MonadCancelThrow](
    sessionPool: Resource[F, Session[F]]
  ): SchemaRepository[F] =
    new SchemaRepository[F] {

      private object codecs {

        val schemaId = skunk.codec.text.text.gimap[SchemaId]

        val schema: Codec[Schema] =
          (schemaId ~
            skunk.circe.codec.json.jsonb)
            .gimap[Schema]

      }

      def insert(schema: Schema): F[Unit] = {
        val q = sql"""insert into schemas (id, schema) values (${codecs.schema})""".command

        sessionPool
          .flatMap(_.prepare(q))
          .use(_.execute(schema))
          .void
          .adaptErr { case SqlState.UniqueViolation(_) => PersistenceError.Conflict }
      }

      def get(id: SchemaId): F[Schema] = sessionPool
        .flatMap {
          _.prepare(
            sql"""select id, schema from schemas where id = ${codecs.schemaId}"""
              .query(codecs.schema)
          )
        }
        .use(_.option(id))
        .flatMap(_.liftTo[F](PersistenceError.NotFound))

    }

}
