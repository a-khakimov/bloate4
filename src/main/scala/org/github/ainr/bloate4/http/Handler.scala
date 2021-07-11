package org.github.ainr.bloate4.http

import cats.effect.Sync
import cats.syntax.all._
import io.circe.generic.auto._
import io.circe.syntax._
import org.github.ainr.bloate4.http.Handler.{MessageResponse, SaveMessageRequest}
import org.github.ainr.bloate4.services.healthcheck.HealthCheckService
import org.github.ainr.bloate4.services.messages.MessagesService
import org.github.ainr.bloate4.services.messages.domain.Message
import org.github.ainr.bloate4.services.version.VersionService
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

trait Handler[F[_]] {
  def routes: HttpRoutes[F]
}

object Handler {

  final case class SaveMessageRequest(message: Message)

  final case class MessageResponse(message: Message)

}

final class HandlerImpl[F[_] : Sync](
  messagesService: MessagesService[F],
  healthCheckService: HealthCheckService[F],
  versionService: VersionService[F]
) extends Handler[F] {

  object dsl extends Http4sDsl[F]
  import dsl._

  override def routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "get_random_message" => {
      for {
        message <- messagesService.getRandomMessage()
        result <- message.map {
          m => Ok(MessageResponse(m).asJson)
        }.getOrElse(NotFound())
      } yield result
    }
    case request@POST -> Root / "save_message" => {
      for {
        v <- request.decodeJson[SaveMessageRequest]
        result <- messagesService.saveMessage(v.message)
        response <- Ok(result.asJson)
      } yield response
    }
    case GET -> Root / "health_check" => {
      Ok(healthCheckService.healthCheck().map(_.asJson))
    }
    case GET -> Root / "version" => {
      Ok(versionService.version())
    }
  }
}
