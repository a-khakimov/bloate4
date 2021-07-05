package org.github.ainr.bloate4.config

import pureconfig.ConfigConvert
import pureconfig.generic.semiauto.deriveConvert

object HttpConfig {

  final case class Config(
    port: Int,
    baseUrl: String
  )

  implicit val convert: ConfigConvert[Config] = deriveConvert
}
