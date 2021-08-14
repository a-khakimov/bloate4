package org.github.ainr.bloate4

import cats.effect.{Async, Blocker, ContextShift, ExitCode, IO, IOApp, Resource}
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.github.ainr.bloate4.cache.MessagesCache.CacheConfig
import org.github.ainr.bloate4.cache.{MessagesCache, MessagesScaffeineCache}
import org.github.ainr.bloate4.config.AppConfig
import org.github.ainr.bloate4.http.HandlerImpl
import org.github.ainr.bloate4.infrastructure.logging.LazyLogging
import org.github.ainr.bloate4.infrastructure.logging.interpreters.{Logger, LoggerWithMetrics}
import org.github.ainr.bloate4.infrastructure.logging.interpreters.Logger.instance
import org.github.ainr.bloate4.infrastructure.metrics.LoggerCounters
import org.github.ainr.bloate4.repositories.fetch.FetchMessages
import org.github.ainr.bloate4.repositories.{MessagesRepo, MessagesRepoDoobieImpl}
import org.github.ainr.bloate4.services.healthcheck.{HealthCheckService, HealthCheckServiceImpl}
import org.github.ainr.bloate4.services.messages.{MessagesService, MessagesServiceImpl}
import org.github.ainr.bloate4.services.version.{VersionService, VersionServiceImpl}
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.metrics.MetricsOps
import org.http4s.metrics.prometheus.{Prometheus, PrometheusExportService}
import org.http4s.server.Router
import org.http4s.server.middleware.{CORS, CORSConfig, Metrics}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt


object Main extends IOApp with LazyLogging {

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      _ <- Logger[IO].info("Application running")
      config <- AppConfig.load[IO]
      _ <- Logger[IO].info(s"${config.http}")
      _ <- Logger[IO].info(s"${config.database}")
      _ <- db.migrate[IO](config.database)
      _ <- resources[IO](config).use {
        case (ec, transactor, metricsService, metrics) => {
          val cacheConfig = CacheConfig(5.second, 500)
          val messagesCache: MessagesCache[IO] = new MessagesScaffeineCache[IO](cacheConfig)

          val loggerCounters = LoggerCounters[IO](metricsService.collectorRegistry)

          val healthCheckServiceLogger = new LoggerWithMetrics[IO](LoggerFactory.getLogger(HealthCheckService.getClass))(loggerCounters)
          val messagesServiceLogger = new LoggerWithMetrics[IO](LoggerFactory.getLogger(MessagesService.getClass))(loggerCounters)
          val versionServiceLogger = new LoggerWithMetrics[IO](LoggerFactory.getLogger(VersionService.getClass))(loggerCounters)
          val messagesRepoLogger = new LoggerWithMetrics[IO](LoggerFactory.getLogger(MessagesRepo.getClass))(loggerCounters)

          val repo: MessagesRepo[IO] = new MessagesRepoDoobieImpl(transactor)(messagesRepoLogger)
          val healthCheckService: HealthCheckService[IO] = new HealthCheckServiceImpl[IO](healthCheckServiceLogger)
          val messagesService: MessagesService[IO] = new MessagesServiceImpl[IO](repo, FetchMessages.source(repo), messagesCache.make)(messagesServiceLogger)
          val versionService: VersionService[IO] = new VersionServiceImpl[IO](versionServiceLogger)

          val handler: http.Handler[IO] = new HandlerImpl[IO](messagesService, healthCheckService, versionService)

          val originConfig = CORSConfig(anyOrigin = true, allowCredentials = true, maxAge = 1.day.toSeconds)

          val router = Router[IO](
            "/api" -> Metrics[IO](metrics)(handler.routes),
            "/" -> metricsService.routes
          )

          val routerWithCORS = CORS(router, originConfig)
          http.server(routerWithCORS.orNotFound)(ec)
        }
      }
    } yield ExitCode.Success
  }

  def resources[F[_]: Async: ContextShift](
    config: AppConfig.Config
  ): Resource[F, (ExecutionContext, HikariTransactor[F], PrometheusExportService[F],  MetricsOps[F])] = {
    for {
      blocker <- Blocker[F]
      metricsService <- PrometheusExportService.build[F]
      metrics <- Prometheus.metricsOps[F](metricsService.collectorRegistry, "server")
      ec <- ExecutionContexts.cachedThreadPool[F]
      transactor <- db.transactor[F](config.database)(ec, blocker)
    } yield (ec, transactor, metricsService, metrics)
  }
}
