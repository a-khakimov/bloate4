package org.github.ainr.bloate4

import cats.data.Kleisli
import cats.effect.{ConcurrentEffect, Timer}
import org.http4s.{Request, Response}
import org.http4s.server.blaze.BlazeServerBuilder

import scala.concurrent.ExecutionContext

package object http {

  def server[F[_]: Timer: ConcurrentEffect](
    service: Kleisli[F, Request[F], Response[F]]
  )(
    ec: ExecutionContext
  ): F[Unit] = {
    BlazeServerBuilder[F](ec)
      .bindHttp(5555, "0.0.0.0")
      .withHttpApp(service)
      .serve
      .compile
      .drain
  }


}
