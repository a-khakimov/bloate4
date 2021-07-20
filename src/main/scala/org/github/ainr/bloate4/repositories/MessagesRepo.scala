package org.github.ainr.bloate4.repositories

import cats.effect.Bracket
import cats.syntax.all._
import doobie.implicits._
import doobie.util.fragment
import doobie.util.transactor.Transactor
import org.github.ainr.bloate4.infrastructure.logging.{Labels, Logger}

trait MessagesRepo[F[_]] {

  def insertMessage(message: String): F[Unit]

  def selectRandomMessage(): F[Option[String]]
}

object MessagesRepo {

}

class MessagesRepoDoobieImpl[F[_]](
  xa: Transactor[F]
)(
  logger: Logger[F] with Labels[F]
)(
  implicit bracket: Bracket[F, Throwable]
) extends MessagesRepo[F] {

  override def insertMessage(message: String): F[Unit] = {
    for {
      _ <- SQL
        .insertMessage(message)
        .update
        .withUniqueGeneratedKeys[Long]("id")
        .transact(xa)
    } yield ()
  }

  override def selectRandomMessage(): F[Option[String]] = {
    for {
      message <- SQL
        .selectRandomMessage
        .query[String]
        .option
        .transact(xa) <* logger.info("get_random_message_DB", "Get random message from DB")
    } yield message
  }
}

object SQL {
  def insertMessage(message: String): fragment.Fragment = {
    sql"""INSERT INTO messages (message) VALUES ($message)"""
  }
  def selectRandomMessage: fragment.Fragment = {
    sql"""SELECT message FROM messages ORDER BY RANDOM() LIMIT 1"""
  }
}
