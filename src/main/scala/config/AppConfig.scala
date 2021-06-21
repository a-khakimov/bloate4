package org.github.ainr.bloate4
package config

import pureconfig._
import pureconfig.generic.semiauto._
import zio.{Layer, ZIO, ZLayer}

object AppConfig {

  final case class Config(
    http: HttpConfig.Config,
    database: DatabaseConfig.Config
  )

  implicit private val convert: ConfigConvert[Config] = deriveConvert

  val live: Layer[IllegalStateException, AppConfig] =
    ZLayer.fromEffect {
      ZIO
        .fromEither(ConfigSource.default.load[Config])
        .mapError(failures => new IllegalStateException(s"Error loading configuration: $failures"))
    }
}
