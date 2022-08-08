package jvs.model

import io.circe.Codec
import io.circe.generic.extras.semiauto._

final case class SchemaId(value: String) extends AnyVal

object SchemaId {
  implicit val codec: Codec[SchemaId] = deriveUnwrappedCodec
}
