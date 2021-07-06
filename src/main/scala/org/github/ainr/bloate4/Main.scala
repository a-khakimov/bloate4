package org.github.ainr.bloate4

import cats.effect.{Async, Blocker, ContextShift, ExitCode, IO, IOApp, Resource, Sync}
import cats.implicits.catsSyntaxApplicativeId
import com.github.blemale.scaffeine.Scaffeine
import com.typesafe.scalalogging.LazyLogging
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import fetch.{Data, DataCache, Fetch}
import org.github.ainr.bloate4.config.AppConfig
import org.github.ainr.bloate4.http.HandlerImpl
import org.github.ainr.bloate4.repositories.fetch.FetchMessages
import org.github.ainr.bloate4.repositories.{MessagesRepo, MessagesRepoDoobieImpl}
import org.github.ainr.bloate4.services.messages.{MessagesService, MessagesServiceImpl}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt


object Main extends IOApp with LazyLogging {

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      _ <- logger.info("Application running").pure[IO]
      config <- AppConfig.load[IO]
      _ <- logger.info(s"${config.http}").pure[IO]
      _ <- logger.info(s"${config.database}").pure[IO]
      _ <- db.migrate[IO](config.database)
      _ <- resources[IO](config).use {
        case (ec, transactor) => {

          val caffeineCache: DataCache[IO] = new DataCache[IO] {

            private val cache =
              Scaffeine()
                .recordStats()
                .expireAfterWrite(10.second)
                .maximumSize(500)
                .build[Any, Any]()

            override def lookup[K, V](i: K, data: Data[K, V]): IO[Option[V]] = {
              IO(cache.getIfPresent(i).map(v => v.asInstanceOf[V]))
            }

            override def insert[I, A](i: I, v: A, data: Data[I, A]): IO[DataCache[IO]] = {
              cache.put(i, v)
              IO(this)
            }
          }
          val repo: MessagesRepo[IO] = new MessagesRepoDoobieImpl(transactor)
          val messagesService: MessagesService[IO] = new MessagesServiceImpl[IO](repo, FetchMessages.source(repo), caffeineCache)
          val handler: http.Handler[IO] = new HandlerImpl[IO](messagesService)

          http.server(handler.routes())(ec)
        }
      }
    } yield ExitCode.Success
  }

  def resources[F[_]: Sync: Async: ContextShift](
    config: AppConfig.Config
  ): Resource[F, (ExecutionContext, HikariTransactor[F])] = {
    for {
      blocker <- Blocker[F]
      ec <- ExecutionContexts.cachedThreadPool[F]
      transactor <- db.transactor[F](config.database)(ec, blocker)
    } yield (ec, transactor)
  }
}
