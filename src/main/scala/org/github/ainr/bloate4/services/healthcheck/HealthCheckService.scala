package org.github.ainr.bloate4.services.healthcheck

import cats.Applicative
import cats.implicits.catsSyntaxApplicativeId
import org.github.ainr.bloate4.infrastructure.logging.Logger
import org.github.ainr.bloate4.infrastructure.logging.interpreters.Logger
import org.github.ainr.bloate4.services.healthcheck.HealthCheckService.HealthCheckData

trait HealthCheckService[F[_]] {
  def healthCheck(): F[HealthCheckData]
}

final class HealthCheckServiceImpl[F[_]: Applicative: Logger] extends HealthCheckService[F] {
  override def healthCheck(): F[HealthCheckData] = {
    Logger[F].info("hello")
    HealthCheckData("Hello, my little pony!").pure[F]
  }
}

object HealthCheckService {
  case class HealthCheckData(message: String)
}
