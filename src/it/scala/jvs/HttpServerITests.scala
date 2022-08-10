package jvs

import weaver._
import jvs.http.HttpServer
import org.http4s.HttpRoutes
import cats.effect.IO
import jvs.http.HttpConfig
import com.comcast.ip4s._
import org.http4s.HttpApp
import org.typelevel.log4cats.noop.NoOpLogger
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.client.dsl.io._
import cats.implicits._
import org.http4s.Method.GET
import io.circe.Json
import org.http4s.circe.CirceEntityCodec._
import org.http4s.Status

object HttpServerITests extends SimpleIOSuite {
  implicit val logger = NoOpLogger.apply[IO]

  test("Global error handler hides exception message") {
    val randomPort = port"0"
    val anException = new Exception("Gory details!")

    val mkServer = HttpServer.run(
      HttpApp.liftF[IO](IO.raiseError(anException)),
      HttpConfig(host"localhost", randomPort),
    )
    val mkClient = EmberClientBuilder.default[IO].build

    (mkServer, mkClient)
      .tupled
      .flatMap { case (server, client) => client.run(GET(server.baseUri)) }
      .use(response => response.as[Json].tupleRight(response.status))
      .map { case (body, status) =>
        assert.eql(body, Json.obj()) &&
        assert.eql(status, Status.InternalServerError)
      }
  }
}
