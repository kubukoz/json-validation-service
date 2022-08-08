package jvs.persistence

import cats.Applicative
import cats.MonadThrow
import cats.effect.kernel.Ref
import cats.implicits._
import io.circe.Json
import jvs.persistence.PersistenceError
import jvs.model._

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

}
