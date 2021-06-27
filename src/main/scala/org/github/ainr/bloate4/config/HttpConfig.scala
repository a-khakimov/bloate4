package org.github.ainr.bloate4.config

import org.github.ainr.bloate4.config.AppConfig.AppConfig
import pureconfig.ConfigConvert
import pureconfig.generic.semiauto.deriveConvert
import zio.{Has, URIO, ZIO, ZLayer}

object HttpConfig {
  type HttpConfig = Has[HttpConfig.Config]

  final case class Config(
    port: Int,
    baseUrl: String
  )

  implicit val convert: ConfigConvert[Config] = deriveConvert

  val fromAppConfig: ZLayer[AppConfig, Nothing, HttpConfig] = ZLayer.fromService(_.http)

  val getHttpConfig: URIO[HttpConfig, HttpConfig.Config] = ZIO.service
}
