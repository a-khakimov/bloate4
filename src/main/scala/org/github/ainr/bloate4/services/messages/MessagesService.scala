package org.github.ainr.bloate4.services.messages

import cats.effect.{Concurrent, Timer}
import cats.syntax.all._
import fetch.{DataCache, DataSource, Fetch}
import org.github.ainr.bloate4.infrastructure.logging.interpreters.Logger
import org.github.ainr.bloate4.infrastructure.logging.{LazyLogging, Logger}
import org.github.ainr.bloate4.repositories.MessagesRepo
import org.github.ainr.bloate4.services.messages.domain.Message

trait MessagesService[F[_]] {

  def saveMessage(message: Message): F[Unit]

  def getRandomMessage(): F[Option[Message]]
}

final class MessagesServiceImpl[F[_] : Concurrent: Timer: Logger](
  repo: MessagesRepo[F],
  fetchMessage: DataSource[F, Int, Message],
  messagesCache: DataCache[F]
) extends MessagesService[F] with LazyLogging {

  override def saveMessage(message: Message): F[Unit] = {
    repo.insertMessage(message)
  }

  override def getRandomMessage(): F[Option[Message]] = {
    Fetch
      .run(Fetch.optional(0, fetchMessage), messagesCache)
      .recoverWith {
        case error => Option("Error").pure[F] <* Logger[F].error("hui", error)
      }
  }
}
