package org.github.ainr.bloate4.repository

import cats.effect.Blocker
import doobie.hikari.HikariTransactor
import doobie.implicits.{toSqlInterpolator, _}
import doobie.util.transactor.Transactor
import org.flywaydb.core.Flyway
import org.github.ainr.bloate4.config.DatabaseConfig
import org.github.ainr.bloate4.config.DatabaseConfig.DatabaseConfig
import zio.blocking.Blocking
import zio.interop.catz._
import zio.{Has, Managed, Task, UIO, URIO, ZIO, ZLayer, ZManaged}

object Repo {
  type Repo = Has[Repo.Service]

  trait Service {
    def get: UIO[Option[Int]]

    def insertMessage(message: String): UIO[Unit]

    def selectRandomMessage(): UIO[Option[String]]
  }

  case class ServiceImpl(xa: Transactor[Task]) extends Service {
    override def get: UIO[Option[Int]] = for {
      n <- sql"select 42".query[Int].option.transact(xa).orDie
    } yield n

    override def insertMessage(message: String): UIO[Unit] = for {
      _ <- sql"INSERT INTO messages (message) VALUES ($message)"
        .update
        .withUniqueGeneratedKeys[Long]("id")
        .transact(xa)
        .orDie
    } yield ()

    override def selectRandomMessage(): UIO[Option[String]] = for {
      message <- sql"SELECT message FROM messages ORDER BY RANDOM() LIMIT 1"
        .query[String]
        .option
        .transact(xa)
        .orDie
    } yield message
  }

  val live: ZLayer[Blocking with DatabaseConfig, Throwable, Repo] = {
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
      } yield ServiceImpl(transactor)
    }
  }

  def repoGet: URIO[Repo, Repo.Service] = ZIO.access(_.get)
}
