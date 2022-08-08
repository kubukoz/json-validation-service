package jvs

import cats.effect.IO
import jvs.http.API
import jvs.http.HttpServer
import jvs.model.SchemaId
import jvs.transport.ActionResult
import jvs.transport.ActionStatus
import org.http4s.Uri
import org.http4s.client.Client
import weaver._
import org.http4s.Status

object RouteTests extends SimpleIOSuite {

  private val validSchemaId = SchemaId("valid schema")
  private val invalidSchemaId = SchemaId("invalid schema")

  private val fakeAPI: API[IO] =
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

    }

  private val http4sClient = Client.fromHttpApp(HttpServer.routes[IO](fakeAPI))

  private val client = API.client(http4sClient, Uri())
  private val clientRaw = API.clientRaw[IO](Uri())

  test("Upload schema successfully") {
    client
      .uploadSchema(validSchemaId, "{}")
      .map { uploadResult =>
        assert(uploadResult.status == ActionStatus.Success)
      }
  }

  test("Upload schema with error response") {
    client
      .uploadSchema(invalidSchemaId, "{")
      .map { uploadResult =>
        assert(
          uploadResult.status == ActionStatus.Error
        )
      }
  }

  test("Upload schema with error response: response has UnprocessableEntity status") {
    http4sClient.status(clientRaw.uploadSchema(invalidSchemaId, "{")).map {
      assert.eql(_, Status.UnprocessableEntity)
    }
  }
}
