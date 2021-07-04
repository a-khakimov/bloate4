package org.github.ainr.bloate4.repositories.fetch

import cats.effect.Concurrent
import fetch.{Data, DataSource}
import org.github.ainr.bloate4.repositories.Repo


object Messages extends Data[Int, String] {

  override def name: String = "Messages"

  def db[F[_]: Concurrent]: DataSource[F, Int, String] = new DataSource[F, Int, String] {

    override def data: Data[Int, String] = Messages

    override implicit def CF: Concurrent[F] = Concurrent[F]

    override def fetch(id: Int): F[Option[String]] = Repo.Service
  }
}
