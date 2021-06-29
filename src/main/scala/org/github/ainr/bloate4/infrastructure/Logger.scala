package org.github.ainr.bloate4.infrastructure

import zio.Has

object Logger {
  type Logger = Has[Logger.Service]

  trait Service {
    def info(message: String): Unit
    def warning(message: String): Unit
    def error(message: String): Unit
  }
}
