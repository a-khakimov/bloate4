package org.github.ainr.bloate4

import cats.effect.{Async, Blocker, ContextShift, Resource}
import doobie.hikari.HikariTransactor
import org.github.ainr.bloate4.config.DatabaseConfig

import scala.concurrent.ExecutionContext

package object db {
  def transactor[F[_]: Async: ContextShift](
    config: DatabaseConfig.Config
  )(
    ec: ExecutionContext,
    blocker: Blocker
  ): Resource[F, HikariTransactor[F]] = {
    HikariTransactor.newHikariTransactor[F](
      config.driver,
      config.url,
      config.user,
      config.password,
      ec,
      blocker
    )
  }
}
