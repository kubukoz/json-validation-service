package jvs.services

import jvs.model._
import cats.Applicative

trait SchemaService[F[_]] {
  def persistSchema(schema: Schema): F[Unit]
}

object SchemaService {
  def apply[F[_]](implicit F: SchemaService[F]): SchemaService[F] = F

  def instance[F[_]: Applicative]: SchemaService[F] =
    new SchemaService[F] {
      def persistSchema(schema: Schema): F[Unit] = Applicative[F].unit
    }

}
