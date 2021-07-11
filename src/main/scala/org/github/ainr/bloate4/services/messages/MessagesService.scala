package org.github.ainr.bloate4.services.messages

import cats.effect.{Concurrent, Sync, Timer}
import cats.implicits._
import fetch.{DataCache, DataSource, Fetch}
import org.github.ainr.bloate4.infrastructure.logging.{Labels, Logger}
import org.github.ainr.bloate4.repositories.MessagesRepo
import org.github.ainr.bloate4.services.messages.MessagesService._
import org.github.ainr.bloate4.services.messages.domain.Message


trait MessagesService[F[_]] {

  def saveMessage(message: Message): F[MessageSavingResult]

  def getRandomMessage(): F[Option[Message]]
}

object MessagesService {

  val validationRegex = "[a-zA-Zа-яА-ЯёЁ\\d\\s.,`'\\[\\]\\(\\)\"]+".r
  val maxMessageLength = 200
  val minMessageLength = 1

  final case class MessageSavingResult(result: String)
  final case class MessageGettingResult(result: String)

  sealed trait MessagesServiceError extends Throwable
  final case object TooLongMessageError extends MessagesServiceError
  final case object TooShortMessageError extends MessagesServiceError
  final case object MessageSymbolsValidationError extends MessagesServiceError

}

final class MessagesServiceImpl[F[_]: Concurrent: Timer](
  repo: MessagesRepo[F],
  fetchMessage: DataSource[F, Int, Message],
  messagesCache: DataCache[F]
)(
  logger: Logger[F] with Labels[F]
) extends MessagesService[F] {

  override def saveMessage(message: Message): F[MessageSavingResult] = {
    val result = for {
      validatedMessage <- validateMessage(message)
      _ <- repo.insertMessage(validatedMessage)
      _ <- logger.info("save_message", s"Save message: [$validatedMessage]")
      result = MessageSavingResult("Сообщение отправлено")
    } yield result
    result.recoverWith(saveMessageErrorHandler(_))
  }

  override def getRandomMessage(): F[Option[Message]] = {
    Fetch
      .run(Fetch.optional(0, fetchMessage), messagesCache)
      .recoverWith {
        case error => Option("Привет, мой маленький пони!").pure[F] <*
          logger.error("get_random_message_error", "Get random message error", error)
      } <* logger.info("get_random_message", "Get random message")
  }

  private def saveMessageErrorHandler(error: Throwable): F[MessageSavingResult] = {
    error match {
      case e: TooLongMessageError.type => MessageSavingResult("Слишком длинное сообщение").pure[F] <*
        logger.error("save_message_error_too_short", "Save message error", e)
      case e: TooShortMessageError.type => MessageSavingResult("Слишком короткое сообщение").pure[F] <*
        logger.error("save_message_error_too_long", "Save message error", e)
      case e: MessageSymbolsValidationError.type => MessageSavingResult("Сообщение содержит недопустимые символы").pure[F] <*
        logger.error("save_message_error_validation", "Save message error", e)
      case e => MessageSavingResult("Сообщение не отправлено").pure[F] <*
        logger.error("save_message_error", "Save message error", e)
    }
  }

  // TODO: Тут лучше заюзать мощь кошачьего валидатора
  private def validateMessage(message: Message): F[Message] = {
    val len = message.length
    if (len < minMessageLength) Sync[F].raiseError(TooShortMessageError)
    else if (len > maxMessageLength) Sync[F].raiseError(TooLongMessageError)
    else if (!validationRegex.pattern.matcher(message).matches()) Sync[F].raiseError(MessageSymbolsValidationError)
    else message.pure[F]
  }
}
