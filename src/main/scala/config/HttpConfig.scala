package org.github.ainr.bloate4
package config

import pureconfig.ConfigConvert
import pureconfig.generic.semiauto.deriveConvert
import zio.ZLayer

object HttpConfig {

  final case class Config(
    port: Int,
    baseUrl: String
  )

  implicit val convert: ConfigConvert[Config] = deriveConvert

  val fromAppConfig: ZLayer[AppConfig, Nothing, HttpConfig] = ZLayer.fromService(_.http)
}
