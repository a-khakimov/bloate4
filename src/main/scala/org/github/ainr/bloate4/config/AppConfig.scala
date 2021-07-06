package org.github.ainr.bloate4.config

import cats.effect.Sync
import cats.syntax.all._
import pureconfig.error.ConfigReaderException
import pureconfig.generic.semiauto.deriveConvert
import pureconfig.{ConfigConvert, ConfigSource}

object AppConfig {

  final case class Config(
    http: HttpConfig.Config,
    database: DatabaseConfig.Config
  )

  implicit private val convert: ConfigConvert[Config] = deriveConvert

  def load[F[_]: Sync]: F[Config] = {
    Sync[F]
      .delay {
        ConfigSource
          .default
          .load[Config]
      }
      .flatMap {
        case Left(e) => Sync[F].raiseError(new ConfigReaderException[Config](e))
        case Right(config) => Sync[F].pure(config)
      }
  }
}
