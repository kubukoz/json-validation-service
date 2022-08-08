package jvs

import cats.effect.IO
import jvs.http.API
import jvs.http.HttpServer
import org.http4s.Uri
import org.http4s.client.Client
import weaver._
import java.util.UUID

object RouteTests extends SimpleIOSuite {

  val anUUID = new UUID(0L, 0L)

  val client = API.client(Client.fromHttpApp(HttpServer.routes[IO](API.server[IO])), Uri())

  test("Upload schema") {
    ResourceUtils
      .readResourceJson("/examples/config-schema.json")
      .flatMap { schema =>
        client.uploadSchema(anUUID, schema)
      }
      .as(success)
  }
}
