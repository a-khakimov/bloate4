package org.github.ainr.bloate4.test.clients

import cats.effect.{BracketThrow, ExitCode, IO, IOApp, Resource}
import cats.syntax.all._
import org.http4s.Status.Successful
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.{Method, Request, Uri}

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext


private[clients] object GetClient extends IOApp {

  val uri = Uri.unsafeFromString(s"http://37.46.128.173:5555/api/get_random_message")

  def doRequest[F[_]: BracketThrow](client: Client[F]): F[Unit] =
    client run Request[F]()
      .withMethod(Method.GET)
      .withUri(uri) use {
      case Successful(_) => ().pure[F]
      case _ => ().pure[F]
    } recover {
      case _ => println("Error")
    }

  def resources(): Resource[IO, Client[IO]] = {
    val ec = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
    for {
      httpClient <- BlazeClientBuilder[IO](ec).resource
    } yield httpClient
  }

  override def run(args: List[String]): IO[ExitCode] = for {
    _ <- resources().use {
      httpClient => {
        lazy val repeat: IO[Unit] = for {
          _ <- doRequest(httpClient)
          //_ <- IO.sleep(50.milliseconds)
          _ <- IO.shift
          _ <- repeat
        } yield ()

        repeat
      }
    }
  } yield ExitCode.Success
}
