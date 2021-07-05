package org.github.ainr.bloate4.services

import cats.Applicative
import cats.implicits.catsSyntaxApplicativeId
import org.github.ainr.bloate4.services.HealthCheckService.HealthCheckData

trait HealthCheckService[F[_]] {
  def healthCheck(): F[HealthCheckData]
}

final class HealthCheckServiceImpl[F[_]: Applicative] extends HealthCheckService[F] {
  override def healthCheck(): F[HealthCheckData] =
    HealthCheckData("Hello, my little pony!").pure[F]
}

object HealthCheckService {
  case class HealthCheckData(message: String)
}
