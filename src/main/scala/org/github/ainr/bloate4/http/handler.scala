package org.github.ainr.bloate4.http

import cats.data.Kleisli
import io.circe._
import io.circe.generic.semiauto._
import org.github.ainr.bloate4.services.MessagesService
import org.http4s._
import org.http4s.implicits._
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
import zio._
import zio.interop.catz._
import zio.logging.Logging

object handler extends Http4sDsl[Task] {

  type Message = String
  final case class MessageRequest(message: Message)
  final case class MessageResponse(message: Option[Message])

  implicit val messageRequestDecoder: Decoder[MessageRequest] = deriveDecoder
  implicit val messageRequestEncoder: Encoder[MessageRequest] = deriveEncoder
  implicit val messageResponseEncoder: Encoder[MessageResponse] = deriveEncoder

  def routes[R <: Logging](messagesService: MessagesService.Service): Kleisli[Task, Request[Task], Response[Task]] =
    HttpRoutes.of[Task] {
      case GET -> Root / "get_random_message" => {
        val response = for {
          message <- messagesService.getRandomMessage()
        } yield MessageResponse(message)
        Ok(response)
      }
      case request @ POST -> Root / "save_message" => {
        request.decode[MessageRequest] { messageRequest =>
          messagesService
            .saveMessage(messageRequest.message)
            .flatMap(a => Created(a))
        }
      }
      case GET -> Root / "health_check" => {
        Ok("Hello, my little pony!\n")
      }
    }.orNotFound
}
