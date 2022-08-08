package jvs

import io.circe.Codec
import io.circe.generic.extras.semiauto._
import io.circe.generic.extras.Configuration
import scala.annotation.nowarn

sealed trait ActionResult extends Product with Serializable

object ActionResult {
  final case class UploadSchema(schemaId: SchemaId, status: ActionStatus) extends ActionResult

  implicit val codec: Codec[ActionResult] = {
    // Used in a macro
    @nowarn("cat=unused")
    implicit val config: Configuration = Configuration.default.withDiscriminator("action")

    deriveConfiguredCodec
  }

}

sealed trait ActionStatus extends Product with Serializable

object ActionStatus {
  case object Success extends ActionStatus

  implicit val codec: Codec[ActionStatus] = deriveEnumerationCodec
}
