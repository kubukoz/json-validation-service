package jvs

import cats.effect.IO
import jvs.http.API
import jvs.http.HttpServer
import org.http4s.Uri
import org.http4s.client.Client
import weaver._

object RouteTests extends SimpleIOSuite {

  val client = API.client(Client.fromHttpApp(HttpServer.routes[IO](API.server[IO])), Uri())

  test("Call hello") {
    client.hello.map {
      assert.eql(_, "Hello World!")
    }
  }
}
