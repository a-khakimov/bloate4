package org.github.ainr.bloate4.services.messages

import cats.Monad
import cats.syntax.all._
import org.github.ainr.bloate4.repositories.MessagesRepo
import org.github.ainr.bloate4.services.messages.domain.Message

trait MessagesService[F[_]] {

  def saveMessage(message: Message): F[Unit]

  def getRandomMessage(): F[Option[Message]]
}

final class MessagesServiceImpl[F[_]: Monad](
  repo: MessagesRepo[F]
) extends MessagesService[F] {

  override def saveMessage(message: Message): F[Unit] = {
    repo.insertMessage(message)
  }

  override def getRandomMessage(): F[Option[Message]] = {
    for {
      message <- repo.selectRandomMessage()
    } yield message
  }
}
