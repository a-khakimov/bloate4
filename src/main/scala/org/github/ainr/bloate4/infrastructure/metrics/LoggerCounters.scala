package org.github.ainr.bloate4.infrastructure.metrics

import cats.Applicative
import cats.syntax.all._
import io.prometheus.client.{CollectorRegistry, Counter}


final case class LoggerCounters[F[_]](
  errorCounter: F[Counter],
  warnCounter: F[Counter],
  infoCounter: F[Counter],
  debugCounter: F[Counter]
)

object LoggerCounters {

  def apply[F[_]: Applicative](registry: CollectorRegistry): LoggerCounters[F] = {
    LoggerCounters(
      errorCounter.register(registry).pure[F],
      warnCounter.register(registry).pure[F],
      infoCounter.register(registry).pure[F],
      debugCounter.register(registry).pure[F],
    )
  }

  private lazy val errorCounter: Counter.Builder =
    Counter
      .build()
      .name("bloate4_error")
      .help("Total error logs")
      .labelNames("label")

  private lazy val warnCounter: Counter.Builder =
    Counter
      .build()
      .name("bloate4_warn")
      .help("Total warning logs")
      .labelNames("label")

  private lazy val infoCounter: Counter.Builder =
    Counter
      .build()
      .name("bloate4_info")
      .help("Total info logs")
      .labelNames("label")

  private lazy val debugCounter: Counter.Builder =
    Counter
      .build()
      .name("bloate4_debug")
      .help("Total debug logs")
      .labelNames("label")
}
