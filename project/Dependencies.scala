import sbt._

object Dependencies {

  object Versions {
    val cats_effect      = "2.5.1"
    val fetch            = "1.3.2"
    val scaffeine        = "4.0.1"
    val doobie           = "0.13.4"
    val pureConfig       = "0.16.0"
    val http4s           = "0.21.24"
    val kindProjector    = "0.13.0"
    val flyway           = "7.8.2"
    val log4j            = "2.14.1"
    val circe            = "0.14.1"
    val logbackClassic   = "1.2.3"
    val scalaLogging     = "3.9.3"
  }

  import Versions._

  val App =
    List(
      "org.typelevel"                %% "cats-effect"         % cats_effect,
      "org.typelevel"                %% "cats-core"           % "2.3.0",
      "com.47deg"                    %% "fetch"               % fetch,
      "com.47deg"                    %% "fetch-debug"         % fetch,
      "com.github.blemale"           %% "scaffeine"           % scaffeine,
      "com.github.pureconfig"        %% "pureconfig"          % pureConfig,
      "io.circe"                     %% "circe-core"          % circe,
      "io.circe"                     %% "circe-generic"       % circe,
      "io.circe"                     %% "circe-parser"        % circe,
      "io.estatico"                  %% "newtype"             % "0.4.4",
      "ch.qos.logback"                % "logback-classic"     % logbackClassic,
      "com.typesafe.scala-logging"   %% "scala-logging"       % scalaLogging,
      "org.tpolecat"                 %% "doobie-core"         % doobie,
      "org.tpolecat"                 %% "doobie-h2"           % doobie,
      "org.tpolecat"                 %% "doobie-postgres"     % doobie,
      "org.tpolecat"                 %% "doobie-hikari"       % doobie,
      "org.http4s"                   %% "http4s-blaze-server" % http4s,
      "org.http4s"                   %% "http4s-circe"        % http4s,
      "org.http4s"                   %% "http4s-dsl"          % http4s,
      "org.flywaydb"                  % "flyway-core"         % flyway
    )
}
