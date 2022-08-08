package jvs.config

import ciris.ConfigDecoder
import com.comcast.ip4s.Host
import com.comcast.ip4s.Port

object ConfigDecoders {

  implicit val hostDecoder: ConfigDecoder[String, Host] =
    ConfigDecoder[String].mapOption("host")(Host.fromString)

  implicit val portDecoder: ConfigDecoder[String, Port] =
    ConfigDecoder[String].mapOption("port")(Port.fromString)

}
