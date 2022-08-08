package jvs

import cats.effect.IO
import io.circe.Json
import cats.implicits._

object ResourceUtils {

  // todo: use for something more like an integration test
  def readResourceJson(path: String): IO[Json] = fs2
    .io
    .readClassResource[IO, ResourceUtils.type](path)
    .through(fs2.text.utf8.decode[IO])
    .compile
    .string
    .flatMap(io.circe.parser.parse(_).liftTo[IO])

}
