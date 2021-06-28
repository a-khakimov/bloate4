package org.github.ainr.bloate4.config

import org.github.ainr.bloate4.config.AppConfig.AppConfig
import pureconfig.ConfigConvert
import pureconfig.generic.semiauto.deriveConvert
import zio.{Has, URIO, ZIO, ZLayer}

object DatabaseConfig {
  type DatabaseConfig = Has[DatabaseConfig.Config]

  final case class Config(
    url: String,
    driver: String,
    user: String,
    password: String
  ) {
    override def toString: String = s"url[$url] driver[$driver] user[$user] password[****]"
  }

  implicit val convert: ConfigConvert[Config] = deriveConvert

  val fromAppConfig: ZLayer[AppConfig, Nothing, DatabaseConfig] =
    ZLayer
      .fromService(appConfig => appConfig.database)

  val getDatabaseConfig: URIO[DatabaseConfig, DatabaseConfig.Config] = ZIO.service
}
