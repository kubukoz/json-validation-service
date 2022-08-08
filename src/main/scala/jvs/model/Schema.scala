package jvs.model

import jvs.model.SchemaId
import io.circe.Json

case class Schema(id: SchemaId, json: Json)
