package org.github.ainr.bloate4.repositories

import cats.effect.Blocker
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.flywaydb.core.Flyway
import org.github.ainr.bloate4.config.DatabaseConfig
import org.github.ainr.bloate4.config.DatabaseConfig.DatabaseConfig
import zio.blocking.Blocking
import zio.interop.catz._
import zio.logging.Logging
import zio.{Has, Managed, Task, UIO, ZIO, ZLayer, ZManaged}

object Repo {
  type Repo = Has[Repo.Service]

  trait Service {

    def insertMessage(message: String): Task[Unit]

    def selectRandomMessage(): UIO[Option[String]]
  }

  object SQL {
    val insertMessage = (message: String) => sql"INSERT INTO messages (message) VALUES ($message)"
    val selectRandomMessage = sql"SELECT message FROM messages ORDER BY RANDOM() LIMIT 1"
  }

  final case class DoobieRepo(xa: Transactor[Task]) extends Service {

    override def insertMessage(message: String): Task[Unit] = for {
      _ <- SQL
        .insertMessage(message)
        .update
        .withUniqueGeneratedKeys[Long]("id")
        .transact(xa)
        .foldM(error => Task.fail(error), _ => Task.succeed(()))
    } yield ()

    override def selectRandomMessage(): UIO[Option[String]] = for {
      message <- SQL
        .selectRandomMessage
        .query[String]
        .option
        .transact(xa)
        .orDie
    } yield message
  }

  val live: ZLayer[Blocking with DatabaseConfig with Logging, Throwable, Repo] = {
    def initDb(cfg: DatabaseConfig.Config): Task[Unit] = {
      Task {
        Flyway
          .configure()
          .dataSource(cfg.url, cfg.user, cfg.password)
          .load()
          .migrate()
      }.unit
    }

    def mkTransactor(
      cfg: DatabaseConfig.Config
    ): ZManaged[Blocking, Throwable, HikariTransactor[Task]] = {
      ZIO.runtime[Blocking].toManaged_.flatMap { implicit rt =>
        for {
          transactEC <- Managed.succeed(
            rt.environment
              .get[Blocking.Service]
              .blockingExecutor
              .asEC
          )
          connectEC = rt.platform.executor.asEC
          transactor <- HikariTransactor
            .newHikariTransactor[Task](
              cfg.driver,
              cfg.url,
              cfg.user,
              cfg.password,
              connectEC,
              Blocker.liftExecutionContext(transactEC)
            )
            .toManaged
        } yield transactor
      }
    }

    ZLayer.fromManaged {
      for {
        cfg <- DatabaseConfig.getDatabaseConfig.toManaged_
        _ <- initDb(cfg).toManaged_
        transactor <- mkTransactor(cfg)
      } yield DoobieRepo(transactor)
    }
  }
}
