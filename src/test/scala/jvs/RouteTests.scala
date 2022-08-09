package jvs

import cats.effect.IO
import cats.implicits._
import io.circe.Json
import jvs.http.API
import jvs.http.HttpServer
import jvs.model.SchemaId
import jvs.transport.ActionResult
import jvs.transport.ActionStatus
import org.http4s.Uri
import org.http4s.client.Client
import weaver._
import org.http4s.Status
import jvs.transport.ActionKind

object RouteTests extends SimpleIOSuite {

  private val validSchemaId = SchemaId("valid schema")
  private val nonExistentSchemaId = SchemaId("schema doesn't exist")
  private val invalidSchemaId = SchemaId("invalid schema")

  private val validSchema = Json.obj()
  private val validJsonDocument = Json.obj()
  private val invalidJsonDocument = Json.fromString("invalid")

  private val fakeServerAPI: API[IO] =
    new API[IO] {

      def uploadSchema(schemaId: SchemaId, schema: String): IO[ActionResult] = {
        val responses = Map(
          validSchemaId -> ActionResult.uploadSchemaSuccess(
            schemaId
          ),
          invalidSchemaId -> ActionResult.uploadSchemaError(
            schemaId,
            message = "Invalid JSON",
          ),
        )

        IO.pure(responses(schemaId))
      }

      def downloadSchema(schemaId: SchemaId): IO[Either[ActionResult, Json]] = {
        val responses = Map(
          validSchemaId -> validSchema.asRight.pure[IO],
          nonExistentSchemaId -> ActionResult
            .downloadSchemaError(schemaId, "Schema not found")
            .asLeft
            .pure[IO],
        )

        responses(schemaId)
      }

      def validateDocument(schemaId: SchemaId, document: Json): IO[ActionResult] = {
        if (document === validJsonDocument)
          ActionResult.validateDocumentSuccess(schemaId)
        else
          ActionResult
            .validateDocumentError(schemaId, "JSON Validation failed")
      }.pure[IO]

    }

  private val http4sClient = Client.fromHttpApp(HttpServer.routes[IO](fakeServerAPI))

  private val client = API.client(http4sClient, Uri())
  private val clientRaw = API.clientRaw[IO](Uri())

  test("Upload schema successfully") {
    client
      .uploadSchema(validSchemaId, "{}")
      .map { uploadResult =>
        assert(uploadResult.isSuccess) &&
        assert(uploadResult.action == ActionKind.UploadSchema)
      }
  }

  test("Upload schema with error response") {
    client
      .uploadSchema(invalidSchemaId, "{")
      .map { uploadResult =>
        assert(uploadResult.status == ActionStatus.Error) &&
        assert(uploadResult.action == ActionKind.UploadSchema)
      }
  }

  test("Upload schema with error response: response has UnprocessableEntity status") {
    http4sClient.status(clientRaw.uploadSchema(invalidSchemaId, "{")).map {
      assert.eql(_, Status.UnprocessableEntity)
    }
  }

  test("Download schema successfully") {
    client
      .downloadSchema(validSchemaId)
      .map { result =>
        assert(result == validSchema.asRight)
      }
  }

  test("Download schema: not found") {
    client
      .downloadSchema(nonExistentSchemaId)
      .map { result =>
        assert(result.isLeft) &&
        assert(result.leftMap(_.status) == ActionStatus.Error.asLeft) &&
        assert(result.leftMap(_.action) == ActionKind.DownloadSchema.asLeft)
      }
  }

  test("Validate document: successful") {
    client
      .validateDocument(validSchemaId, validJsonDocument)
      .map { validateResult =>
        assert(validateResult.isSuccess) &&
        assert(validateResult.action == ActionKind.ValidateDocument)
      }
  }

  test("Validate document: failed") {
    client
      .validateDocument(validSchemaId, invalidJsonDocument)
      .map { validateResult =>
        assert(validateResult.status == ActionStatus.Error) &&
        assert(validateResult.action == ActionKind.ValidateDocument)
      }
  }
}
