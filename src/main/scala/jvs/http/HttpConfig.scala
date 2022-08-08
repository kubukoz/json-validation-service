package jvs.http

import cats.implicits._
import ciris.ConfigValue
import com.comcast.ip4s._
import jvs.config.ConfigDecoders._

final case class HttpConfig(host: Host, port: Port)

object HttpConfig {

  def config[F[_]]: ConfigValue[F, HttpConfig] = {
    import ciris._

    (
      env("HTTP_HOST").as[Host].default(host"0.0.0.0"),
      env("HTTP_PORT").as[Port].default(port"4000"),
    ).parMapN(apply)
  }

}
