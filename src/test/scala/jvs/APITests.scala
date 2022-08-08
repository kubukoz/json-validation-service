package jvs

import cats.effect.IO
import jvs.errors.AppError
import jvs.http.API
import jvs.model.Schema
import jvs.model.SchemaId
import jvs.services.SchemaService
import jvs.transport.ActionStatus
import org.typelevel.log4cats.noop.NoOpLogger
import weaver._

object APITests extends SimpleIOSuite {
  private implicit val logger = NoOpLogger[IO]

  private val aSchemaId = SchemaId("a schema id")

  private implicit val schemaService: SchemaService[IO] =
    new SchemaService[IO] {

      def persistSchema(schema: Schema): IO[Unit] =
        if (schema.id.value.contains("exists"))
          IO.raiseError(AppError.SchemaAlreadyExists)
        else
          IO.unit

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
    val perform = api.uploadSchema(SchemaId("already exists"), "{}")

    perform.map { result =>
      assert(result.status == ActionStatus.Error) &&
      assert(result.message == Some("Schema already exists"))
    }
  }
}
