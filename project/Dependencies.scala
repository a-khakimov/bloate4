import sbt._

object Dependencies {

  object Versions {
    val zio              = "1.0.9"
    val zioInteropCats   = "2.4.1.0"
    val doobie           = "0.13.4"
    val pureConfig       = "0.16.0"
    val http4s           = "0.21.24"
    val kindProjector    = "0.13.0"
    val flyway           = "7.8.2"
    val zioLogging       = "0.5.8"
    val log4j            = "2.14.1"
    val circe            = "0.14.1"
    val logbackClassic   = "1.2.3"
    val scalaLogging     = "3.9.3"
  }

  import Versions._

  val App =
    List(
      "com.47deg"                    %% "fetch"               % "1.3.2",
      "com.github.blemale"           %% "scaffeine"           % "4.0.1",
      "com.github.pureconfig"        %% "pureconfig"          % pureConfig,
      "dev.zio"                      %% "zio-test-sbt"        % zio   % "test",
      "dev.zio"                      %% "zio-test"            % zio   % "test",
      "dev.zio"                      %% "zio"                 % zio,
      "io.circe"                     %% "circe-core"          % circe,
      "io.circe"                     %% "circe-generic"       % circe,
      "dev.zio"                      %% "zio-interop-cats"    % zioInteropCats,
      "ch.qos.logback"                % "logback-classic"     % logbackClassic,
      "com.typesafe.scala-logging"   %% "scala-logging"       % scalaLogging,
      "org.tpolecat"                 %% "doobie-core"         % doobie,
      "org.tpolecat"                 %% "doobie-h2"           % doobie,
      "org.tpolecat"                 %% "doobie-postgres"     % doobie,
      "org.tpolecat"                 %% "doobie-hikari"       % doobie,
      "org.http4s"                   %% "http4s-blaze-server" % http4s,
      "org.http4s"                   %% "http4s-circe"        % http4s,
      "org.http4s"                   %% "http4s-dsl"          % http4s,
      "io.github.kitlangton"         %% "zio-magic"           % "0.3.2",
      "org.flywaydb"                  % "flyway-core"         % flyway,
      compilerPlugin(("org.typelevel" % "kind-projector"      % kindProjector).cross(CrossVersion.full))
    )
}
