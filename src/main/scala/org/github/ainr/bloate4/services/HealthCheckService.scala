package org.github.ainr.bloate4.services

import zio.{Has, Task, UIO, URIO, ZIO, ZLayer}

object HealthCheckService {
  type HealthCheckService = Has[HealthCheckService.Service]

  case class HealthCheckData(message: String)

  trait Service {
    def healthCheck(): UIO[HealthCheckData]
  }

  object Service {
    val live: Service = new Service() {
      override def healthCheck(): UIO[HealthCheckData] =
        Task
          .succeed(
            HealthCheckData("Hello, my little pony!")
          )
    }
  }

  val live: ZLayer[Any, Throwable, HealthCheckService] = ZLayer.succeed(HealthCheckService.Service.live)

  def access: URIO[HealthCheckService, HealthCheckService.Service] = ZIO.service
}
