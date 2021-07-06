package org.github.ainr.bloate4.services

import org.github.ainr.bloate4.repositories.MessagesRepo

trait MessagesService[F[_]] {

  def saveMessage(message: String): F[Unit]

  def getRandomMessage(): F[Option[String]]
}

final class MessagesServiceImpl[F[_]](
  repo: MessagesRepo[F]
) extends MessagesService[F] {

  override def saveMessage(message: String): F[Unit] = {
    repo.insertMessage(message)
  }

  override def getRandomMessage(): F[Option[String]] = {
    repo.selectRandomMessage()
  }
}
