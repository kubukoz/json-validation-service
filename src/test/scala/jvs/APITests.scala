package jvs

import weaver._
import jvs.http.API
import cats.effect.IO
import jvs.model.SchemaId
import jvs.transport.ActionStatus
import org.typelevel.log4cats.noop.NoOpLogger

object APITests extends SimpleIOSuite {
  private implicit val logger = NoOpLogger[IO]

  private val aSchemaId = SchemaId("a schema id")
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
}
