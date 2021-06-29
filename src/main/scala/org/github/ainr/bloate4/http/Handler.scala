package org.github.ainr.bloate4.http

import cats.data.Kleisli
import com.typesafe.scalalogging.LazyLogging
import io.circe._
import io.circe.generic.semiauto._
import org.github.ainr.bloate4.services.HealthCheckService.Service.live.healthCheck
import org.github.ainr.bloate4.services.HealthCheckService.{HealthCheckData, HealthCheckService}
import org.github.ainr.bloate4.services.MessagesService
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
  final case class SaveMessageRequest(message: Message)
  final case class MessageResponse(message: Option[Message])

  implicit val messageRequestDecoder: Decoder[SaveMessageRequest] = deriveDecoder
  implicit val messageRequestEncoder: Encoder[SaveMessageRequest] = deriveEncoder

  implicit val healthCheckDataDecoder: Decoder[HealthCheckData] = deriveDecoder
  implicit val healthCheckDataEncoder: Encoder[HealthCheckData] = deriveEncoder

  implicit val messageResponseEncoder: Encoder[MessageResponse] = deriveEncoder

  final class ServiceImpl(
    messagesService: MessagesService.Service
  ) extends Service with LazyLogging {

    object dsl extends Http4sDsl[Task]
    import dsl._

    override def routes(): Kleisli[Task, Request[Task], Response[Task]] = HttpRoutes.of[Task] {
      case GET -> Root / "get_random_message" => {
        val response = for {
          message <- messagesService.getRandomMessage() <* ZIO.succeed(logger.info(s"Get random message request"))
        } yield MessageResponse(message)
        Ok(response)
      }
      case request @ POST -> Root / "save_message" => {
        val response = request.decode[SaveMessageRequest] { req =>
          if (req.message.length > 20)
            PayloadTooLarge()
          else
            messagesService
              .saveMessage(req.message)
              .foldM(cause => {
                logger.error(s"Message saving error", cause)
                NoContent()
              },
                Created(_)
              )
        }
        response
      }
      case GET -> Root / "health_check" => Ok(healthCheck())
    }.orNotFound
  }

  val live: ZLayer[MessagesService with HealthCheckService, Throwable, Handler] =
    ZLayer.fromService( messagesService =>
      new ServiceImpl(messagesService)
    )

  def service: URIO[Handler, Handler.Service] = ZIO.service
}
