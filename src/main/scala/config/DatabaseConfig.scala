package org.github.ainr.bloate4
package config

import pureconfig.ConfigConvert
import pureconfig.generic.semiauto.deriveConvert
import zio.ZLayer

object DatabaseConfig {

  final case class Config(
    url: String,
    driver: String,
    user: String,
    password: String
  )

  implicit val convert: ConfigConvert[Config] = deriveConvert

  val fromAppConfig: ZLayer[AppConfig, Nothing, DatabaseConfig] =
    ZLayer
      .fromService(appConfig => appConfig.database)
}
