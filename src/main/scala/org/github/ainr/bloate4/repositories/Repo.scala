package org.github.ainr.bloate4.repositories

import cats.effect.Bracket
import doobie.Transactor
import doobie.implicits._
import doobie.util.fragment

trait MessagesRepo[F[_]] {

  def insertMessage(message: String): F[Unit]

  def selectRandomMessage(): F[Option[String]]
}

object SQL {
  def insertMessage(message: String): fragment.Fragment = {
    sql"""
        |INSERT INTO messages (message) VALUES ($message)
       """
      .stripMargin
  }
  def selectRandomMessage: fragment.Fragment = {
    sql"""SELECT message FROM messages ORDER BY RANDOM() LIMIT 1"""
  }
}

class MessagesRepoDoobieImpl[F[_]: Bracket[*[_], Throwable]](
  xa: Transactor[F]
) extends MessagesRepo[F] {

  override def insertMessage(message: String): F[Unit] = {

    val _ =
      SQL
      .insertMessage(message)
        .update
        .withUniqueGeneratedKeys[Long]("id")
        .attemptSqlState
      .transact(xa)

    ???
  }

  override def selectRandomMessage(): F[Option[String]] = {
    ???
  }
}
