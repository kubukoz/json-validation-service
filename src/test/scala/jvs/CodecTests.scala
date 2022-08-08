package jvs

import cats.implicits._
import io.circe.literal._
import io.circe.syntax._
import jvs.model.SchemaId
import jvs.transport.ActionResult
import jvs.transport.ActionStatus
import weaver._

object CodecTests extends FunSuite {
  test("ActionResult.UploadSchema") {
    val input: ActionResult = ActionResult.UploadSchema(
      SchemaId("a schema"),
      ActionStatus.Success,
      message = Some("a message"),
    )

    val expected =
      json"""{
        "action": "uploadSchema",
        "schemaId": "a schema",
        "status": "success",
        "message": "a message"
      }"""

    assert.eql(input.asJson, expected) &&
    assert(input.asRight == expected.as[ActionResult])
  }
}