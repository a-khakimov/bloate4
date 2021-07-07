package org.github.ainr.bloate4.repositories

import cats.effect.Bracket
import cats.syntax.all._
import com.typesafe.scalalogging.LazyLogging
import doobie.implicits._
import doobie.util.fragment
import doobie.util.transactor.Transactor

trait MessagesRepo[F[_]] {

  def insertMessage(message: String): F[Unit]

  def selectRandomMessage(): F[Option[String]]
}

class MessagesRepoDoobieImpl[F[_]](
  xa: Transactor[F]
)(
  implicit bracket: Bracket[F, Throwable]
) extends MessagesRepo[F] with LazyLogging {

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
        .transact(xa)
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
