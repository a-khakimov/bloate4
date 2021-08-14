package org.github.ainr.bloate4.repositories.fetch

import cats.syntax.all._
import cats.effect.Concurrent
import fetch.{Data, DataSource}
import org.github.ainr.bloate4.infrastructure.logging.interpreters.Logger
import org.github.ainr.bloate4.infrastructure.logging.interpreters.Logger._
import org.github.ainr.bloate4.infrastructure.logging.LazyLogging
import org.github.ainr.bloate4.repositories.MessagesRepo
import org.github.ainr.bloate4.services.messages.domain.Message


object FetchMessages extends Data[Int, Message] {

  override def name: String = "FetchMessages"

  def source[F[_]: Concurrent](
    repo: MessagesRepo[F]
  ): DataSource[F, Int, Message] =

    new DataSource[F, Int, Message] with LazyLogging {

    override def data: Data[Int, Message] = FetchMessages

    override def CF: Concurrent[F] = Concurrent[F]

    override def fetch(id: Int): F[Option[Message]] = {
      repo.selectRandomMessage() <* Logger[F].info(s"Fetch message from repo $id")
    }
  }
}
