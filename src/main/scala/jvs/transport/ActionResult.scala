package jvs.transport

import cats.implicits._
import io.circe.Codec
import io.circe.Decoder
import io.circe.generic.extras.semiauto._
import io.circe.syntax._
import jvs.http.CirceConfig._

import jvs.model.SchemaId

final case class ActionResult(
  action: ActionKind,
  schemaId: SchemaId,
  status: ActionStatus,
  message: Option[String],
)

object ActionResult {

  def uploadSchemaSuccess(
    schemaId: SchemaId
  ): ActionResult = ActionResult(
    ActionKind.UploadSchema,
    schemaId,
    ActionStatus.Success,
    message = None,
  )

  def uploadSchemaError(
    schemaId: SchemaId,
    message: String,
  ): ActionResult = ActionResult(
    ActionKind.UploadSchema,
    schemaId,
    ActionStatus.Error,
    message = Some(message),
  )

  implicit val codec: Codec[ActionResult] = deriveConfiguredCodec

}

sealed trait ActionKind extends Product with Serializable

object ActionKind {
  case object UploadSchema extends ActionKind

  implicit val codec: Codec[ActionKind] = Codec.from(
    Decoder[String].emap {
      case "uploadSchema" => UploadSchema.asRight
      case other          => other.asLeft
    },
    { case UploadSchema => "uploadSchema".asJson },
  )

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
