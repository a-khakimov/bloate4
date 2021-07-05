package org.github.ainr.bloate4

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.catsSyntaxApplicativeId
import com.typesafe.scalalogging.LazyLogging


object Main extends IOApp with LazyLogging {


//  def runHttp(
//    httpApp: Kleisli[Task, Request[Task], Response[Task]],
//    port: Int
//  ): ZIO[R, Throwable, Unit] = {
//    ZIO.runtime[R].flatMap { implicit rts =>
//      val ec = rts.platform.executor.asEC
//      BlazeServerBuilder
//        .apply[Task](ec)
//        .bindHttp(port, "0.0.0.0")
//        .withHttpApp(httpApp)
//        .serve
//        .compile
//        .drain
//    }
//  }

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      _ <- logger.info("Hello").pure[IO]
    } yield ExitCode.Success
  }
}
