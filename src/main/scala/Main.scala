package org.github.ainr.bloate4

import config.{AppConfig, DatabaseConfig}

import cats.effect.{Blocker, ExitCode}
import doobie.hikari.HikariTransactor
import doobie.util.transactor.Transactor
import org.flywaydb.core.Flyway
import org.http4s.HttpApp
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import zio.blocking.Blocking
import zio.clock._
import zio.console._
import zio.interop.catz._
import zio.{Has, Managed, RIO, Task, UIO, URIO, ZIO, ZLayer, ZManaged}


object Repo {
  trait Service {
    def get: UIO[Int]
  }

  case class ServiceImpl(xa: Transactor[Task]) extends Service {
    override def get: UIO[Int] = UIO(42)
  }

  val live: ZLayer[Blocking with DatabaseConfig, Throwable, Has[Repo.Service]] = {
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
          connectEC   = rt.platform.executor.asEC
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

    ZLayer.fromService[DatabaseConfig, Has[Repo.Service]] {
      (databaseConfig) => for {
        _          <- initDb(databaseConfig.get).toManaged_
        transactor <- mkTransactor(databaseConfig.get)
      } yield ServiceImpl(transactor)
    }
  }

  def repoGet: URIO[Has[Repo.Service], Repo.Service] = ZIO.access(_.get)
}

object Main extends zio.App {

  //type AppEnv = Console with Clock with AppConfig with Repo.Repo

  //type L1 = Blocking with Console with Clock
  //type L2 = L1 with AppConfig with HttpConfig with DatabaseConfig
  //type L3 = L2 with Repo.Repo
  //type AppEnv = L3

  //val l1: ZLayer[Blocking, Throwable, Blocking with Console with Clock] = AppConfig.live ++ Blocking.any ++ Console.live ++ Clock.live
  //val l2: ZLayer[Blocking with Console with Clock, Throwable, DatabaseConfig] = DatabaseConfig.fromAppConfig
  //val l3: ZLayer[L2, Throwable, Repo.Repo] = (Repo.live)

  //val repoLayer = DatabaseConfig.fromAppConfig ++ Repo.live

  type AppTask[A] = RIO[Console with Clock, A]

  val program = for {
    _ <- putStrLn(s"Welcome to ZIO!")
    i <- Repo.repoGet
    _ <- putStrLn(s"$i")
    config <- config.getAppConfig
    _ <- putStrLn(s"${config.http}")
    _ <- putStrLn(s"${config.database}")
    httpApp = Router[AppTask](config.http.baseUrl -> http.handler.routes()).orNotFound
    _ <- runHttp(httpApp, config.http.port)
    } yield zio.ExitCode.success

  val program2 = for {
    _ <- putStrLn(s"Welcome to ZIO!").provideLayer(Console.live)
    repo <- Repo.repoGet.provideLayer(AppConfig.live ++ Repo.live)
    _ <- putStrLn(s"${repo.get}").provideLayer(Console.live)
  } yield zio.ExitCode.success

  def run(args: List[String]): URIO[zio.ZEnv, zio.ExitCode] = {
    //program
    //  .provideLayer(Blocking.any ++ Clock.live ++ Console.live ++ AppConfig.live)
    //  .exitCode

    program2.exitCode
  }

  def runHttp[R <: Clock](
    httpApp: HttpApp[RIO[R, *]],
    port: Int
  ): ZIO[R, Throwable, Unit] = {
    type Task[A] = RIO[R, A]
    ZIO.runtime[R].flatMap { implicit rts =>
      val ec = rts.platform.executor.asEC
      BlazeServerBuilder
        .apply[Task](ec)
        .bindHttp(port, "0.0.0.0")
        .withHttpApp(CORS(httpApp))
        .serve
        .compile[Task, Task, ExitCode]
        .drain
    }
  }
}