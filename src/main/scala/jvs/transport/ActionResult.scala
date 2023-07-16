package jvs.transport

import cats.implicits._
import io.circe.Codec
import io.circe.Decoder
import io.circe.syntax._
import jvs.model.SchemaId

final case class ActionResult(
  action: ActionKind,
  id: SchemaId,
  status: ActionStatus,
  message: Option[String],
) derives Codec.AsObject {
  def isSuccess = status == ActionStatus.Success
}

object ActionResult {

  def uploadSchemaSuccess(
    schemaId: SchemaId
  ): ActionResult = ActionResult(
    ActionKind.UploadSchema,
    schemaId,
    ActionStatus.Success,
    message = None,
  )

  def validateDocumentSuccess(
    schemaId: SchemaId
  ): ActionResult = ActionResult(
    ActionKind.ValidateDocument,
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

  def downloadSchemaError(
    schemaId: SchemaId,
    message: String,
  ): ActionResult = ActionResult(
    ActionKind.DownloadSchema,
    schemaId,
    ActionStatus.Error,
    message = Some(message),
  )

  def validateDocumentError(
    schemaId: SchemaId,
    message: String,
  ): ActionResult = ActionResult(
    ActionKind.ValidateDocument,
    schemaId,
    ActionStatus.Error,
    message = Some(message),
  )

}

sealed trait ActionKind extends Product with Serializable

object ActionKind {
  case object UploadSchema extends ActionKind
  case object DownloadSchema extends ActionKind
  case object ValidateDocument extends ActionKind

  implicit val codec: Codec[ActionKind] = Codec.from(
    Decoder[String].emap {
      case "uploadSchema"     => UploadSchema.asRight
      case "downloadSchema"   => DownloadSchema.asRight
      case "validateDocument" => ValidateDocument.asRight
      case other              => other.asLeft
    },
    {
      case UploadSchema     => "uploadSchema".asJson
      case DownloadSchema   => "downloadSchema".asJson
      case ValidateDocument => "validateDocument".asJson
    },
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
