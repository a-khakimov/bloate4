package org.github.ainr.bloate4.http

import cats.data.Kleisli
import io.circe._
import io.circe.generic.semiauto._
import org.github.ainr.bloate4.services.MessagesService.MessagesService
import org.http4s._
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import zio._
import zio.interop.catz._

object Handler {
  type Handler = Has[Handler.Service]

  trait Service {
    def routes(): Kleisli[Task, Request[Task], Response[Task]]
  }

  type Message = String
  final case class MessageRequest(message: Message)
  final case class MessageResponse(message: Option[Message])

  implicit val messageRequestDecoder: Decoder[MessageRequest] = deriveDecoder
  implicit val messageRequestEncoder: Encoder[MessageRequest] = deriveEncoder
  implicit val messageResponseEncoder: Encoder[MessageResponse] = deriveEncoder

  val live: ZLayer[MessagesService, Throwable, Handler] = ZLayer.fromService {
    messagesService => {
      object dsl extends Http4sDsl[Task]
      import dsl._

      new Service() {
        override def routes(): Kleisli[Task, Request[Task], Response[Task]] =
          HttpRoutes.of[Task] {
            case GET -> Root / "get_random_message" => {
              val response = for {
                message <- messagesService.getRandomMessage()
              } yield MessageResponse(message)
              Ok(response)
            }
            case request @ POST -> Root / "save_message" => {
              val response = request.decode[MessageRequest] { messageRequest =>
                messagesService
                  .saveMessage(messageRequest.message)
                  .flatMap(a => Created(a))
              }
              response
            }
            case GET -> Root / "health_check" => {
              Ok("Hello, my little pony!\n")
            }
          }.orNotFound
      }
    }
  }


  def service: URIO[Handler, Handler.Service] = ZIO.service
}
