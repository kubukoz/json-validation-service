package jvs.transport

import cats.implicits._
import io.circe.Codec
import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import io.circe.syntax._
import jvs.http.CirceConfig

import scala.annotation.nowarn
import jvs.model.SchemaId

sealed trait ActionResult extends Product with Serializable

object ActionResult {
  final case class UploadSchema(schemaId: SchemaId, status: ActionStatus, message: Option[String])
    extends ActionResult

  implicit val codec: Codec[ActionResult] = {

    // Used in a macro
    @nowarn("cat=unused")
    implicit val configuration: Configuration = CirceConfig
      .base
      .withDiscriminator("action")

    deriveConfiguredCodec
  }

}

sealed trait ActionStatus extends Product with Serializable

object ActionStatus {
  case object Success extends ActionStatus
  case object Error extends ActionStatus

  implicit val codec: Codec[ActionStatus] = Codec.from(
    Decoder[String].emap {
      case "success" => Success.asRight
      case "error"   => Error.asRight
      case other     => other.asLeft
    },
    {
      case Success => "success".asJson
      case Error   => "error".asJson
    },
  )

}