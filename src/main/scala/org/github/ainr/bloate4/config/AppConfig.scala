package org.github.ainr.bloate4.config

import pureconfig.generic.semiauto.deriveConvert
import pureconfig.{ConfigConvert, ConfigSource}
import zio.{Has, Layer, URIO, ZIO, ZLayer}

object AppConfig {
  type AppConfig = Has[AppConfig.Config]

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

  val getAppConfig: URIO[AppConfig, AppConfig.Config] = ZIO.access(_.get)
}
