package org.github.ainr.bloate4.http

import cats.effect.Sync
import cats.syntax.all._
import io.circe.generic.auto._
import io.circe.syntax._
import org.github.ainr.bloate4.http.HandlerImpl.{MessageResponse, SaveMessageRequest, SaveMessageResponse}
import org.github.ainr.bloate4.services.messages.MessagesService
import org.github.ainr.bloate4.services.messages.domain.Message
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

trait Handler[F[_]] {
  def routes: HttpRoutes[F]
}

final class HandlerImpl[F[_] : Sync](
  messagesService: MessagesService[F]
) extends Handler[F] {

  object dsl extends Http4sDsl[F]
  import dsl._

  override def routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "get_random_message" => {
      for {
        message <- messagesService.getRandomMessage()
        result <- message.map(m => Ok(MessageResponse(m).asJson)).getOrElse(NotFound())
      } yield result
    }
    case request@POST -> Root / "save_message" => {
      val c = for {
        v <- request.decodeJson[SaveMessageRequest]
        _ <- messagesService.saveMessage(v.message)
        response = SaveMessageResponse("Ok")
      } yield response
      Ok(c)
    }
    case GET -> Root / "health_check" => {
      Ok("hui")
    }
    case GET -> Root / "version" => {
      Ok("0.0.1")
    }
  }
}

object HandlerImpl {

  final case class SaveMessageRequest(message: Message)

  final case class SaveMessageResponse(result: String)

  final case class MessageResponse(message: Message)

}

