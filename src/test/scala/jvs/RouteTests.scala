package jvs

import cats.effect.IO
import io.circe.Json
import jvs.http.API
import jvs.http.HttpServer
import jvs.model.SchemaId
import jvs.transport.ActionResult
import jvs.transport.ActionStatus
import org.http4s.Uri
import org.http4s.client.Client
import weaver._

object RouteTests extends SimpleIOSuite {

  private val validSchemaId = SchemaId("valid schema")
  private val invalidSchemaId = SchemaId("invalid schema")
  private val duplicateSchemaId = SchemaId("duplicate schema")

  private val fakeAPI =
    new API[IO] {

      def uploadSchema(schemaId: SchemaId, schema: Json): IO[ActionResult] = {
        val responses = Map(
          validSchemaId -> ActionStatus.Success
        )

        IO.pure(ActionResult.UploadSchema(schemaId, responses(schemaId)))
      }

    }

  private val client = API.client(Client.fromHttpApp(HttpServer.routes[IO](fakeAPI)), Uri())

  test("Upload schema") {
    ResourceUtils
      .readResourceJson("/examples/config-schema.json")
      .flatMap { schema =>
        client.uploadSchema(validSchemaId, schema)
      }
      .map { uploadResult =>
        assert(uploadResult == ActionResult.UploadSchema(validSchemaId, ActionStatus.Success))
      }
  }

  test("Upload invalid schema") {
    ignore("todo") *>
      ResourceUtils
        .readResourceJson("/examples/config-schema.json")
        .flatMap { schema =>
          client.uploadSchema(invalidSchemaId, schema)
        }
        .as(success)
  }
}
