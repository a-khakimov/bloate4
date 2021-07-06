package org.github.ainr.bloate4

import cats.Applicative
import cats.effect.{Async, Blocker, ContextShift, Resource}
import cats.implicits.catsSyntaxApplicativeId
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
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

  def migrate[F[_]: Applicative](config: DatabaseConfig.Config): F[MigrateResult] = {
    Flyway
      .configure()
      .dataSource(config.url, config.user, config.password)
      .load()
      .migrate().pure[F]
  }
}
