package jvs.e2e

import cats.effect.IO
import cats.effect.Resource
import cats.effect.std.UUIDGen
import cats.implicits._
import io.circe.literal._
import org.http4s.Method._
import org.http4s.Status
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.Client
import org.http4s.client.dsl.io._
import org.http4s.client.middleware.Retry
import org.http4s.client.middleware.RetryPolicy
import org.http4s.ember.client.EmberClientBuilder
import weaver._

import scala.concurrent.duration._

object E2ETests extends IOSuite {
  type Res = (Client[IO], E2EConfig)

  val sharedResource: Resource[IO, Res] =
    (
      EmberClientBuilder
        .default[IO]
        .build
        .map {
          Retry(
            RetryPolicy(backoff =
              RetryPolicy.exponentialBackoff(maxWait = 5.seconds, maxRetry = 10)
            )
          )
        },
      E2EConfig.config[IO].resource[IO],
    ).tupled

  test("Upload sample schema & validate sample document against it") { case (client, config) =>
    for {
      schemaId <- UUIDGen[IO].randomUUID

      schema <- ResourceUtils.readResourceJson("/examples/config-schema.json")
      createResult <- client.status(POST(config.baseUrl / "schema" / schemaId).withEntity(schema))
      _ <- assert.eql(createResult, Status.Created).failFast[IO]

      document <- ResourceUtils.readResourceJson("/examples/config.json")
      validateResult <- client.status(
        POST(config.baseUrl / "validate" / schemaId).withEntity(document)
      )
    } yield assert.eql(validateResult, Status.Ok)
  }
}
