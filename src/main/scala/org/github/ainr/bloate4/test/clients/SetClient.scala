package org.github.ainr.bloate4.test.clients

import cats.Applicative
import cats.effect.{BracketThrow, ExitCode, IO, IOApp, Resource}
import cats.syntax.all._
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import org.http4s.Status.Successful
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.{Method, Request, Uri}

import java.util.Random
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import scala.util.Random.javaRandomToRandom

private[clients] object SetClient extends IOApp {

  type Message = String
  final case class SaveMessageRequest(message: Message)

  val uri: Uri = Uri.unsafeFromString(s"http://37.46.128.173:5555/api/save_message")

  def generateRandomMessage[F[_]: Applicative](): F[Message] = {
    (new Random()).alphanumeric.filter(_.isLetterOrDigit).take(150).mkString.pure[F]
  }

  def doRequest[F[_]: BracketThrow](message: Message)(client: Client[F]): F[Unit] = {
    client run Request[F]()
      .withMethod(Method.POST)
      .withEntity(SaveMessageRequest(message).asJson)
      .withUri(uri) use {
      case Successful(_) => ().pure[F]
      case _ => ().pure[F]
    } recover {
      case _ => println("Error")
    }
  }

  def resources(): Resource[IO, Client[IO]] = {
    val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))
    for {
      httpClient <- BlazeClientBuilder[IO](ec).resource
    } yield httpClient
  }

  override def run(args: List[String]): IO[ExitCode] = for {
    _ <- resources().use {
      httpClient => {
        lazy val repeat: IO[Unit] = for {
          message <- generateRandomMessage[IO]()
          _ <- doRequest(message)(httpClient)
          //_ <- IO.sleep(100.milliseconds)
          _ <- IO.shift
          _ <- repeat
        } yield ()
        repeat
      }
    }
  } yield ExitCode.Success
}
