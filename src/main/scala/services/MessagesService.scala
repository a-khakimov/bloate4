package org.github.ainr.bloate4
package services

import repository.Repo.Repo

import zio.{Has, UIO, URIO, ZIO, ZLayer}

object MessagesService {
  type MessagesService = Has[MessagesService.Service]

  trait Service {
    def saveMessage(message: String): UIO[Unit]
    def getRandomMessage(): UIO[Option[String]]
  }

  val live: ZLayer[Repo, Throwable, MessagesService] = ZLayer.fromService {
    repo => new Service {
      override def saveMessage(message: String): UIO[Unit] = repo.insertMessage(message)
      override def getRandomMessage(): UIO[Option[String]] = repo.selectRandomMessage()
    }
  }

  val access: URIO[MessagesService, MessagesService.Service] = ZIO.access(_.get)
}
