package jvs.services

import cats.MonadThrow
import cats.implicits._
import jvs.services.AppError
import jvs.persistence.PersistenceError
import jvs.model._
import jvs.persistence.SchemaRepository

trait SchemaService[F[_]] {
  def persistSchema(schema: Schema): F[Unit]
  def getSchema(id: SchemaId): F[Schema]
}

object SchemaService {
  def apply[F[_]](implicit F: SchemaService[F]): SchemaService[F] = F

  def instance[F[_]: SchemaRepository: MonadThrow]: SchemaService[F] =
    new SchemaService[F] {

      def persistSchema(schema: Schema): F[Unit] = SchemaRepository[F].insert(schema).adaptErr {
        case PersistenceError.Conflict => AppError.SchemaAlreadyExists
      }

      def getSchema(id: SchemaId): F[Schema] = SchemaRepository[F].get(id).adaptErr {
        case PersistenceError.NotFound => AppError.SchemaNotFound
      }

    }

}
