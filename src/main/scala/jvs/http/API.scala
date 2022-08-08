package jvs.http

import cats.Monad
import cats.effect.Resource
import cats.effect.kernel.Async
import jvs.http.HttpConfig
import org.http4s.HttpApp
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import cats.Applicative
import cats.effect.Concurrent
import cats.implicits._
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.Method._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.circe.middleware.JsonDebugErrorHandler
import util.chaining._
import org.http4s.client.dsl.Http4sClientDsl

trait API[F[_]] {
  def hello: F[String]
}

object API {

  def apply[F[_]](implicit F: API[F]): API[F] = F

  def server[F[_]: Applicative]: API[F] =
    new API[F] {
      def hello: F[String] = "Hello World!".pure[F]
    }

  def client[F[_]: Concurrent](client: Client[F], baseUrl: Uri): API[F] =
    new API[F] {
      private val dsl = new Http4sClientDsl[F] {}
      import dsl._

      def hello: F[String] = client.fetchAs[String](GET(baseUrl / "hello"))
    }

}
