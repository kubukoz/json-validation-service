package jvs

import cats.effect.IO
import jvs.http.API
import jvs.http.HttpServer
import jvs.model.SchemaId
import jvs.transport.ActionResult
import jvs.transport.ActionStatus
import org.http4s.Uri
import org.http4s.client.Client
import cats.implicits._
import weaver._
import jvs.transport.ActionKind

object RouteTests extends SimpleIOSuite {

  private val validSchemaId = SchemaId("valid schema")
  private val invalidSchemaId = SchemaId("invalid schema")

  private val fakeAPI: API[IO] =
    new API[IO] {

      def uploadSchema(schemaId: SchemaId, schema: String): IO[ActionResult] = {
        val responses = Map(
          validSchemaId -> ActionResult(
            action = ActionKind.UploadSchema,
            schemaId,
            status = ActionStatus.Success,
            message = None,
          ),
          invalidSchemaId -> ActionResult(
            action = ActionKind.UploadSchema,
            schemaId,
            status = ActionStatus.Error,
            message = "Invalid JSON".some,
          ),
        )

        IO.pure(responses(schemaId))
      }

    }

  private val client = API.client(Client.fromHttpApp(HttpServer.routes[IO](fakeAPI)), Uri())

  test("Upload schema") {
    client
      .uploadSchema(validSchemaId, "{}")
      .map { uploadResult =>
        val expected = ActionResult(
          action = ActionKind.UploadSchema,
          validSchemaId,
          status = ActionStatus.Success,
          message = None,
        )

        assert(uploadResult == expected)
      }
  }

  test("Upload non-JSON schema") {
    client
      .uploadSchema(invalidSchemaId, "{")
      .map { uploadResult =>
        val expected = ActionResult(
          action = ActionKind.UploadSchema,
          invalidSchemaId,
          status = ActionStatus.Error,
          message = "Invalid JSON".some,
        )

        assert(
          uploadResult == expected
        )
      }
  }
}
