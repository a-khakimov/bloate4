package org.github.ainr.bloate4.http

import cats.{Defer, Monad}
import cats.data.Kleisli
import com.typesafe.scalalogging.LazyLogging
import io.circe._
import io.circe.generic.semiauto._
import org.github.ainr.bloate4.services.HealthCheckService.HealthCheckData
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT

object Handler {

  trait Service[F[_]] {
    def routes(): Kleisli[F, Request[F], Response[F]]
  }

  type Message = String
  final case class SaveMessageRequest(message: Message)
  final case class MessageResponse(message: Option[Message])

  implicit val messageRequestDecoder: Decoder[SaveMessageRequest] = deriveDecoder
  implicit val messageRequestEncoder: Encoder[SaveMessageRequest] = deriveEncoder

  implicit val healthCheckDataDecoder: Decoder[HealthCheckData] = deriveDecoder
  implicit val healthCheckDataEncoder: Encoder[HealthCheckData] = deriveEncoder

  implicit val messageResponseEncoder: Encoder[MessageResponse] = deriveEncoder

  final class ServiceImpl[F[_]: Monad : Defer]() extends Service[F] with LazyLogging {

    object dsl extends Http4sDsl[F]
    import dsl._

    override def routes(): Kleisli[F, Request[F], Response[F]] = HttpRoutes.of[F] {
      case GET -> Root / "get_random_message" => {
        Ok()
      }
      case _ @ POST -> Root / "save_message" => {
        Ok()
      }
      case GET -> Root / "health_check" => {
        Ok()
      }
    }.orNotFound
  }
}
