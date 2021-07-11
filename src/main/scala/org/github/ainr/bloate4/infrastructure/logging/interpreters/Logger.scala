package org.github.ainr.bloate4.infrastructure.logging.interpreters

import cats.Applicative
import cats.syntax.all._
import org.github.ainr.bloate4.infrastructure.logging.Logger
import org.slf4j.{LoggerFactory, Logger => slf4jLogger}

import scala.language.implicitConversions

object Logger {

  def apply[F[_]](implicit logger: Logger[F]): Logger[F] = logger

  implicit def instance[F[_]: Applicative](
    clazz: Class[_]
  )(
    implicit logger: slf4jLogger = LoggerFactory.getLogger(clazz)
  ): Logger[F] = new Logger[F] {

    def error(msg: String)                    : F[Unit] = logger.error(msg     ).pure[F]

    def error(msg: String, err: Throwable)    : F[Unit] = logger.error(msg, err).pure[F]

    def warn (msg: String)                    : F[Unit] = logger.warn (msg     ).pure[F]

    def info (msg: String)                    : F[Unit] = logger.info (msg     ).pure[F]

    def debug(msg: String)                    : F[Unit] = logger.debug(msg     ).pure[F]
  }

  implicit def instance[F[_]: Applicative](
    implicit logger: slf4jLogger
  ): Logger[F] = new Logger[F] {

    def error(msg: String)                    : F[Unit] = logger.error(msg     ).pure[F]

    def error(msg: String, err: Throwable)    : F[Unit] = logger.error(msg, err).pure[F]

    def warn (msg: String)                    : F[Unit] = logger.warn (msg     ).pure[F]

    def info (msg: String)                    : F[Unit] = logger.info (msg     ).pure[F]

    def debug(msg: String)                    : F[Unit] = logger.debug(msg     ).pure[F]
  }
}

