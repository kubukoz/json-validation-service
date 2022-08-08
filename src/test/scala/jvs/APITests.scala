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

object APITests extends SimpleIOSuite {
  private implicit val logger = NoOpLogger[IO]

  private val aSchema = Json.obj()
  private val aSchemaId = SchemaId("a schema id")
  private val anExistingSchemaId = SchemaId("a schema that exists")

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

    }

  private val api = API.server[IO]

  test("Upload schema successful") {
    api.uploadSchema(aSchemaId, "{}").map { result =>
      assert(result.status == ActionStatus.Success)
    }
  }

  test("Upload schema fails: invalid JSON") {
    api.uploadSchema(aSchemaId, "invalid json").map { result =>
      assert(result.status == ActionStatus.Error) &&
      assert(result.message == Some("Invalid JSON"))
    }
  }

  test("Upload schema fails: underlying service fails") {
    val perform = api.uploadSchema(anExistingSchemaId, "{}")

    perform.map { result =>
      assert(result.status == ActionStatus.Error) &&
      assert(result.message == Some("Schema already exists"))
    }
  }

  test("Download schema: success") {
    api
      .downloadSchema(anExistingSchemaId)
      .map { result =>
        assert(result == Some(aSchema))
      }
  }

  test("Download schema: nonexistent entry") {
    api
      .downloadSchema(aSchemaId)
      .map { result =>
        assert(result.isEmpty)
      }
  }
}
