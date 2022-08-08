package jvs.e2e

import cats.effect.IO

import cats.effect.Resource
import cats.implicits._
import org.http4s.Method._
import org.http4s.Status
import org.http4s.client.Client
import org.http4s.client.dsl.io._
import org.http4s.ember.client.EmberClientBuilder
import weaver._

object E2ETests extends IOSuite {
  type Res = (Client[IO], E2EConfig)

  val sharedResource: Resource[IO, Res] =
    (
      EmberClientBuilder.default[IO].build,
      E2EConfig.config[IO].resource[IO],
    ).tupled

  test("Service responds to GET request") { case (client, config) =>
    val request = GET(config.baseUrl / "hello")

    client.status(request).map {
      assert.eql(_, Status.Ok)
    }
  }
}
