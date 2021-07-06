package org.github.ainr.bloate4.config

import pureconfig.ConfigConvert
import pureconfig.generic.semiauto.deriveConvert

object HttpConfig {

  final case class Config(
    port: Int,
    baseUrl: String
  ) {
    override def toString: String = s"Http configuration: port[$port] baseUrl[$baseUrl]"
  }

  implicit val convert: ConfigConvert[Config] = deriveConvert
}
