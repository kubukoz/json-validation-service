package jvs

import cats.implicits._
import io.circe.literal._
import io.circe.syntax._
import jvs.model.SchemaId
import jvs.transport.ActionResult
import jvs.transport.ActionStatus
import weaver._
import jvs.transport.ActionKind

object CodecTests extends FunSuite {
  test("ActionResult") {
    val input: ActionResult = ActionResult(
      action = ActionKind.UploadSchema,
      SchemaId("a schema"),
      status = ActionStatus.Success,
      message = Some("a message"),
    )

    val expected =
      json"""{
        "action": "uploadSchema",
        "id": "a schema",
        "status": "success",
        "message": "a message"
      }"""

    assert.eql(input.asJson, expected) &&
    assert(input.asRight == expected.as[ActionResult])
  }
}
