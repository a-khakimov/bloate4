package org.github.ainr.bloate4.http

import org.http4s._
import io.circe._
import io.circe.generic.semiauto._
import org.github.ainr.bloate4.services.MessagesService
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
import zio.RIO
import zio.interop.catz._

object handler {

  type Message = String

  final case class MessageRequest(message: Message)

  final case class MessageResponse(message: Option[Message])

  implicit val messageRequestDecoder: Decoder[MessageRequest] = deriveDecoder
  implicit val messageRequestEncoder: Encoder[MessageRequest] = deriveEncoder
  implicit val messageResponseEncoder: Encoder[MessageResponse] = deriveEncoder

  def routes[R](messagesService: MessagesService.Service): HttpRoutes[RIO[R, *]] = {
    type IO[A] = RIO[R, A]

    val dsl = Http4sDsl[IO]
    import dsl._

    HttpRoutes.of[IO] {
      case GET -> Root / "get_random_message" => {
        val response = for {
          message <- messagesService.getRandomMessage()
        } yield MessageResponse(message)
        Ok(response)
      }
      case req@POST -> Root / "save_message" => {
        req.decode[MessageRequest] { messageRequest =>
          messagesService
            .saveMessage(messageRequest.message)
            .flatMap(Created(_))
        }
      }
      case GET -> Root / "health_check" => {
        Ok("Hello, my little pony!\n")
      }
    }
  }
}
