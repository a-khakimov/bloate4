package org.github.ainr.bloate4.infrastructure.logging

trait Labels[F[_]] {

  def error(label: String, msg: String): F[Unit]

  def error(label: String, msg: String, err: Throwable): F[Unit]

  def warn(label: String, msg: String): F[Unit]

  def info(label: String, msg: String): F[Unit]

  def debug(label: String, msg: String): F[Unit]
}
