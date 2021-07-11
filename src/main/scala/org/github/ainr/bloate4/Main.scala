package org.github.ainr.bloate4

import cats.effect.{Async, Blocker, ContextShift, ExitCode, IO, IOApp, Resource}
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.github.ainr.bloate4.cache.MessagesCache.CacheConfig
import org.github.ainr.bloate4.cache.{MessagesCache, MessagesScaffeineCache}
import org.github.ainr.bloate4.config.AppConfig
import org.github.ainr.bloate4.http.HandlerImpl
import org.github.ainr.bloate4.infrastructure.logging.LazyLogging
import org.github.ainr.bloate4.infrastructure.logging.interpreters.Logger
import org.github.ainr.bloate4.infrastructure.logging.interpreters.Logger.instance
import org.github.ainr.bloate4.repositories.fetch.FetchMessages
import org.github.ainr.bloate4.repositories.{MessagesRepo, MessagesRepoDoobieImpl}
import org.github.ainr.bloate4.services.messages.{MessagesService, MessagesServiceImpl}
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.metrics.MetricsOps
import org.http4s.metrics.prometheus.{Prometheus, PrometheusExportService}
import org.http4s.server.Router
import org.http4s.server.middleware.Metrics

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
          val cacheConfig = CacheConfig(10.second, 500)
          val messagesCache: MessagesCache[IO] = new MessagesScaffeineCache[IO](cacheConfig)
          val repo: MessagesRepo[IO] = new MessagesRepoDoobieImpl(transactor)
          val messagesService: MessagesService[IO] = new MessagesServiceImpl[IO](repo, FetchMessages.source(repo), messagesCache.make)
          val handler: http.Handler[IO] = new HandlerImpl[IO](messagesService)
          val router = Router[IO](
            "/api" -> Metrics[IO](metrics)(handler.routes),
            "/" -> metricsService.routes
          )
          http.server(router.orNotFound)(ec)
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
