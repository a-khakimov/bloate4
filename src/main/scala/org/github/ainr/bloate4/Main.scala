package org.github.ainr.bloate4

import cats.effect.ExitCode
import org.github.ainr.bloate4.config.{AppConfig, DatabaseConfig}
import org.github.ainr.bloate4.repository.Repo
import org.github.ainr.bloate4.services.MessagesService
import org.http4s.HttpApp
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import zio.blocking.Blocking
import zio.clock._
import zio.console._
import zio.interop.catz._
import zio.logging.slf4j.Slf4jLogger
import zio.{RIO, URIO, ZIO, logging}
import zio.magic._


object Main extends zio.App {

  type AppTask[A] = RIO[Console with Clock, A]

  val program = for {
    _ <- logging.log.info(s"Welcome to bloate4!")
    config <- AppConfig.getAppConfig
    _ <- logging.log.info(s"Http configs - ${config.http}")
    _ <- logging.log.info(s"Database configs - ${config.database}")
    messagesService <- MessagesService.access
    httpApp = Router[AppTask](config.http.baseUrl -> http.handler.routes(messagesService)).orNotFound
    _ <- runHttp(httpApp, config.http.port)
  } yield zio.ExitCode.success

  def run(args: List[String]): URIO[zio.ZEnv, zio.ExitCode] = {
    program
      .inject(
        Slf4jLogger.make((_, msg) => msg),
        Blocking.live,
        Console.live,
        Clock.live,
        AppConfig.live,
        DatabaseConfig.fromAppConfig,
        MessagesService.live,
        Repo.live)
      .exitCode
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
