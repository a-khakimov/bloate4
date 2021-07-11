package org.github.ainr.bloate4.infrastructure.logging


trait Logger[F[_]] {

  def error(msg: String): F[Unit]

  def error(msg: String, err: Throwable): F[Unit]

  def warn(msg: String): F[Unit]

  def info(msg: String): F[Unit]

  def debug(msg: String): F[Unit]
}
