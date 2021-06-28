package org.github.ainr.bloate4.services

import org.github.ainr.bloate4.repositories.Repo.Repo
import zio.{Has, Task, UIO, URIO, ZIO, ZLayer}

object MessagesService {
  type MessagesService = Has[MessagesService.Service]

  trait Service {
    def saveMessage(message: String): Task[Unit]

    def getRandomMessage(): UIO[Option[String]]
  }

  val live: ZLayer[Repo, Throwable, MessagesService] = ZLayer.fromService {
    repo =>
      new Service {
        override def saveMessage(message: String): Task[Unit] = {
          repo
            .insertMessage(message)
        }

        override def getRandomMessage(): UIO[Option[String]] = {
          repo
            .selectRandomMessage()
        }
      }
  }

  val service: URIO[MessagesService, MessagesService.Service] = ZIO.service
}