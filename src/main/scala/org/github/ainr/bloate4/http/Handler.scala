package org.github.ainr.bloate4.http

import cats.data.Kleisli
import cats.syntax.all._
import cats.{Defer, Monad, MonadError}
import com.typesafe.scalalogging.LazyLogging
import org.http4s._
import org.http4s.implicits._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import org.github.ainr.bloate4.services.MessagesService

import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}


  trait Handler[F[_]] {
    def routes(): Kleisli[F, Request[F], Response[F]]
  }

  final class HandlerImpl[F[_]: Monad : Defer](
    messagesService: MessagesService[F]
  ) extends Handler[F] with LazyLogging {

    type Message = String
    final case class SaveMessageRequest(message: Message)
    final case class MessageResponse(message: Message)

    object dsl extends Http4sDsl[F]
    import dsl._

    override def routes(): Kleisli[F, Request[F], Response[F]] = HttpRoutes.of[F] {
      case GET -> Root / "get_random_message" => {
        for {
          message <- messagesService.getRandomMessage()
          result <- message.map(m => Ok(MessageResponse(m).asJson)).getOrElse(NotFound())
        } yield result
      }
      case request @ POST -> Root / "save_message" => {
        for {
          v <- request.as[SaveMessageRequest]
          r <- messagesService.saveMessage(v.message)
        } yield r
      }
      case GET -> Root / "health_check" => {
        Ok("hui")
      }
      case GET -> Root / "version" => {
        Ok("0.0.1")
      }
    }.orNotFound
  }

