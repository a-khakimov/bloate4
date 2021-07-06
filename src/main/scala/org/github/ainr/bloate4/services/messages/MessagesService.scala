package org.github.ainr.bloate4.services.messages

import cats.Monad
import cats.effect.{Concurrent, Timer}
import cats.syntax.all._
import fetch.{DataCache, DataSource, Fetch, fetchM}
import org.github.ainr.bloate4.repositories.MessagesRepo
import org.github.ainr.bloate4.services.messages.domain.Message

trait MessagesService[F[_]] {

  def saveMessage(message: Message): F[Unit]

  def getRandomMessage(): F[Option[Message]]
}

final class MessagesServiceImpl[F[_] : Monad: Concurrent: Timer](
  repo: MessagesRepo[F],
  fetchMessage: DataSource[F, Int, Message],
  messagesCache: DataCache[F]
) extends MessagesService[F] {

  override def saveMessage(message: Message): F[Unit] = {
    repo.insertMessage(message)
  }

  override def getRandomMessage(): F[Option[Message]] = {
    val c = for {
      message <- Fetch.optional(0, fetchMessage)
      r = message
    } yield r

    Fetch.run(c, messagesCache)
  }
}
