package org.github.ainr.bloate4.config

import pureconfig.ConfigReader.Result
import pureconfig.generic.semiauto.deriveConvert
import pureconfig.{ConfigConvert, ConfigSource}

object AppConfig {

  final case class Config(
    http: HttpConfig.Config,
    database: DatabaseConfig.Config
  )

  implicit private val convert: ConfigConvert[Config] = deriveConvert

  def load: Result[Config] = ConfigSource.default.load[Config]
}
