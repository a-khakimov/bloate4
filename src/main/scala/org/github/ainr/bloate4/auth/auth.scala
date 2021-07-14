package org.github.ainr.bloate4.auth

import cats.Applicative
import cats.data._
import cats.effect._
import cats.implicits._
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.io._
import org.http4s.headers.Authorization
import org.http4s.server.AuthMiddleware
import org.http4s.server.middleware.authentication.BasicAuth
import org.reactormonk.{CryptoBits, PrivateKey}

object auth {

  case class User(id: Long, name: String)

  val key = PrivateKey(scala.io.Codec.toUTF8("hello"))
  val crypto = CryptoBits(key)

  def middleware[F[_]]: AuthMiddleware[F, User] = AuthMiddleware.apply(authUser)

  val authedService: AuthedRoutes[User, IO] =
    AuthedRoutes.of {
      case GET -> Root / "welcome" as user => Ok(s"Welcome, ${user.name}")
    }

  //def retrieveUser1[F[_]]: Kleisli[F, Long, User] = Kleisli[F, Long, User](id => Forbidden(id))

  private val retrieveUser: Kleisli[F, SubClaim, Either[String, UserIds]] =
    Kleisli(subClaim => fromBearerTokenClaim(subClaim).map(_.toRight("User doesn't exist")))


  def authUser[F[_]: Applicative]: Kleisli[F, Request[F], Either[String, User]] =
    Kleisli {
      request =>
        val message = for {
          header <- request.headers.get(Authorization).toRight("Couldn't find an Authorization header")
          token <- crypto.validateSignedToken(header.value).toRight("Invalid token")
          message <- Either.catchOnly[NumberFormatException](token.toLong).leftMap(_.toString)
        } yield message
        message.traverse(retrieveUser.run)
    }
}
