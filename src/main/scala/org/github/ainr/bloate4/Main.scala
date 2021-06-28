package org.github.ainr.bloate4

import cats.data.Kleisli
import com.typesafe.scalalogging.LazyLogging
import org.github.ainr.bloate4.config.AppConfig.AppConfig
import org.github.ainr.bloate4.config.{AppConfig, DatabaseConfig}
import org.github.ainr.bloate4.http.Handler
import org.github.ainr.bloate4.http.Handler.Handler
import org.github.ainr.bloate4.repositories.Repo
import org.github.ainr.bloate4.services.{HealthCheckService, MessagesService}
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.{Request, Response}
import zio.blocking.Blocking
import zio.clock._
import zio.interop.catz._
import zio.interop.catz.implicits._
import zio.magic._
import zio.{ExitCode, Task, URIO, ZIO}


object Main extends zio.App with LazyLogging {

  val program: ZIO[Clock with Handler with AppConfig, Throwable, ExitCode] = for {
    _ <- ZIO.succeed(logger.info(s"Welcome to bloate4!"))
    config <- AppConfig.getAppConfig
    _ <- ZIO.succeed(logger.info(s"Http configs - ${config.http}"))
    _ <- ZIO.succeed(logger.info(s"Database configs - ${config.database}"))
    handler <- Handler.service
    httpApp = handler.routes()
    _ <- runHttp(httpApp, config.http.port)
  } yield zio.ExitCode.success

  def run(args: List[String]): URIO[zio.ZEnv, zio.ExitCode] = {
    program
      .inject(
        Blocking.live,
        Clock.live,
        AppConfig.live,
        DatabaseConfig.fromAppConfig,
        MessagesService.live,
        HealthCheckService.live,
        Handler.live,
        Repo.live)
      .exitCode
  }

  def runHttp[R <: Clock](
    httpApp: Kleisli[Task, Request[Task], Response[Task]],
    port: Int
  ): ZIO[R, Throwable, Unit] = {
    ZIO.runtime[R].flatMap { implicit rts =>
      val ec = rts.platform.executor.asEC
      BlazeServerBuilder
        .apply[Task](ec)
        .bindHttp(port, "0.0.0.0")
        .withHttpApp(httpApp)
        .serve
        .compile
        .drain
    }
  }
}
