package org.github.ainr.bloate4.config

import pureconfig.ConfigConvert
import pureconfig.generic.semiauto.deriveConvert

object DatabaseConfig {

  final case class Config(
    url: String,
    driver: String,
    user: String,
    password: String
  ) {
    override def toString: String = s"url[$url] driver[$driver] user[$user] password[****]"
  }

  implicit val convert: ConfigConvert[Config] = deriveConvert
}
