package jvs.services

import cats.MonadThrow
import cats.implicits._
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.github.fge.jsonschema.main.JsonSchemaFactory
import io.circe.Json
import io.circe.JsonNumber
import jvs.model._
import jvs.persistence.PersistenceError
import jvs.persistence.SchemaRepository
import jvs.services.AppError

import scala.jdk.CollectionConverters._

trait SchemaService[F[_]] {
  def persistSchema(schema: Schema): F[Unit]
  def getSchema(id: SchemaId): F[Schema]
  def validateDocument(schemaId: SchemaId, document: Json): F[Unit]
}

object SchemaService {
  def apply[F[_]](implicit F: SchemaService[F]): SchemaService[F] = F

  def instance[F[_]: SchemaRepository: MonadThrow]: SchemaService[F] =
    new SchemaService[F] {
      private val jsonSchemaFactory = JsonSchemaFactory.byDefault()

      def persistSchema(schema: Schema): F[Unit] = SchemaRepository[F].insert(schema).adaptErr {
        case PersistenceError.Conflict => AppError.SchemaAlreadyExists
      }

      def getSchema(id: SchemaId): F[Schema] = SchemaRepository[F].get(id).adaptErr {
        case PersistenceError.NotFound => AppError.SchemaNotFound
      }

      def validateDocument(
        schemaId: SchemaId,
        document: Json,
      ): F[Unit] =
        getSchema(schemaId)
          .map(schema => circeToJackson(schema.json))
          .map(
            jsonSchemaFactory.getJsonSchema(_).validate(circeToJackson(document.deepDropNullValues))
          )
          .ensureOr { report =>
            AppError.InvalidDocument {
              report.iterator().asScala.toList.map { report =>
                report.getMessage
              }
            }
          }(_.isSuccess())
          .void

    }

  private def circeToJackson(circeJson: Json): JsonNode = {
    val nf = JsonNodeFactory.instance

    def circeNumberToJackson(
      num: JsonNumber
    ): JsonNode = new ObjectMapper().readTree(num.toString())

    circeJson.fold(
      jsonNull = nf.nullNode(),
      jsonBoolean = nf.booleanNode(_),
      jsonNumber = circeNumberToJackson(_),
      jsonString = nf.textNode(_),
      jsonArray =
        _.foldLeft(nf.arrayNode()) { (a, item) =>
          a.add(circeToJackson(item))
        },
      jsonObject =
        _.toList.foldLeft(nf.objectNode()) { case (o, (key, item)) =>
          o.set(key, circeToJackson(item))
        },
    )
  }

}
