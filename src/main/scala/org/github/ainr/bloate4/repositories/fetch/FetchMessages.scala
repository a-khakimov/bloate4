package org.github.ainr.bloate4.repositories.fetch

import cats.effect.Concurrent
import com.typesafe.scalalogging.LazyLogging
import fetch.{Data, DataSource}
import org.github.ainr.bloate4.repositories.MessagesRepo
import org.github.ainr.bloate4.services.messages.domain.Message


object FetchMessages extends Data[Int, String] with LazyLogging {

  override def name: String = "FetchMessages"

  def source[F[_]: Concurrent](repo: MessagesRepo[F]): DataSource[F, Int, Message] =

    new DataSource[F, Int, Message] {

    override def data: Data[Int, Message] = FetchMessages

    override def CF: Concurrent[F] = Concurrent[F]

    override def fetch(id: Int = 0): F[Option[Message]] = {
      logger.info("fetch")
      repo.selectRandomMessage()
    }
  }
}
