package jvs.model

import io.circe.Codec

import cats.implicits.*

final case class SchemaId(value: String)

object SchemaId {

  implicit val codec: Codec[SchemaId] =
    Codec
      .from(
        io.circe.Decoder[String],
        io.circe.Encoder[String],
      )
      .imap(SchemaId(_))(_.value)

}
