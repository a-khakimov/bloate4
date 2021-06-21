package org.github.ainr.bloate4
package http

import org.http4s._
import org.http4s.dsl.Http4sDsl
import zio.RIO
import zio.interop.catz._

object handler {

  def routes[R](): HttpRoutes[RIO[R, *]] = {
    type IO[A] = RIO[R, A]

    val dsl = Http4sDsl[IO]
    import dsl._

    HttpRoutes.of[IO] {
      case GET -> Root / "get" => {
        Ok("Hello, my little pony!")
      }
      case _ @ POST -> Root / "post" => {
        Ok()
      }
    }
  }
}
