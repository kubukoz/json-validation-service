package jvs.http

import cats.implicits._
import ciris.ConfigValue
import com.comcast.ip4s._

final case class HttpConfig(host: Host, port: Port)

object HttpConfig {

  def config[F[_]]: ConfigValue[F, HttpConfig] = {
    import ciris._

    implicit val hostDecoder: ConfigDecoder[String, Host] =
      ConfigDecoder[String].mapOption("host")(Host.fromString)

    implicit val portDecoder: ConfigDecoder[String, Port] =
      ConfigDecoder[String].mapOption("port")(Port.fromString)

    (
      env("HTTP_HOST").as[Host].default(host"0.0.0.0"),
      env("HTTP_PORT").as[Port].default(port"4000"),
    ).parMapN(apply)
  }

}
