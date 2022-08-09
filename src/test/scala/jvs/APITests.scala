package jvs

import cats.effect.IO
import jvs.services.AppError
import jvs.http.API
import jvs.model.Schema
import jvs.model.SchemaId
import jvs.services.SchemaService
import jvs.transport.ActionStatus
import org.typelevel.log4cats.noop.NoOpLogger
import weaver._
import io.circe.Json
import cats.implicits._
import jvs.transport.ActionKind

object APITests extends SimpleIOSuite {
  private implicit val logger = NoOpLogger[IO]

  private val aSchema = Json.obj()
  private val anExistingSchemaId = SchemaId("a schema that exists")
  private val nonExistentSchemaId = SchemaId("a schema that doesn't exist")

  private val aValidDocument = Json.obj()
  private val anInvalidDocument = Json.fromString("invalid document")

  private implicit val schemaService: SchemaService[IO] =
    new SchemaService[IO] {

      def persistSchema(schema: Schema): IO[Unit] =
        if (schema.id.value.contains("exists"))
          IO.raiseError(AppError.SchemaAlreadyExists)
        else
          IO.unit

      def getSchema(id: SchemaId): IO[Schema] =
        if (id.value.contains("exists"))
          IO.pure(Schema(id, aSchema))
        else
          IO.raiseError(AppError.SchemaNotFound)

      def validateDocument(schemaId: SchemaId, document: Json): IO[Unit] =
        getSchema(schemaId) *> {
          if (document == aValidDocument)
            IO.unit
          else
            IO.raiseError(AppError.InvalidDocument(List("expected string")))
        }

    }

  private val api = API.server[IO]

  test("Upload schema successful") {
    api.uploadSchema(nonExistentSchemaId, "{}").map { result =>
      assert(result.status == ActionStatus.Success)
    }
  }

  test("Upload schema fails: invalid JSON") {
    api.uploadSchema(nonExistentSchemaId, "invalid json").map { result =>
      assert(result.status == ActionStatus.Error) &&
      assert.eql(result.message, Some("Invalid JSON"))
    }
  }

  test("Upload schema fails: underlying service fails") {
    val perform = api.uploadSchema(anExistingSchemaId, "{}")

    perform.map { result =>
      assert(result.status == ActionStatus.Error) &&
      assert.eql(result.message, Some("Schema already exists"))
    }
  }

  test("Download schema: success") {
    api
      .downloadSchema(anExistingSchemaId)
      .map { result =>
        assert(result == aSchema.asRight)
      }
  }

  test("Download schema: nonexistent entry") {
    api
      .downloadSchema(nonExistentSchemaId)
      .map { result =>
        assert(result.isLeft) &&
        assert(result.leftMap(_.status) == Left(ActionStatus.Error)) &&
        assert(result.leftMap(_.action) == Left(ActionKind.DownloadSchema))
      }
  }

  test("Validate document: success") {
    api
      .validateDocument(anExistingSchemaId, aValidDocument)
      .map { result =>
        assert(result.status == ActionStatus.Success)
      }
  }

  test("Validate document: invalid document") {
    api
      .validateDocument(anExistingSchemaId, anInvalidDocument)
      .map { result =>
        assert(result.status == ActionStatus.Error) &&
        assert.eql(result.message, Some("Invalid document: expected string"))
      }
  }

  test("Validate document: schema not found") {
    api
      .validateDocument(nonExistentSchemaId, aValidDocument)
      .map { result =>
        assert(result.status == ActionStatus.Error) &&
        assert.eql(result.message, Some("Schema not found"))
      }
  }
}
