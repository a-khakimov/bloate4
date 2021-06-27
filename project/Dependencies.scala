import sbt._

object Dependencies {

  object Versions {
    val zio              = "1.0.9"
    val zioInteropCats   = "2.4.1.0"
    val doobie           = "0.13.2"
    val pureConfig       = "0.15.0"
    val http4s           = "0.21.22"
    val kindProjector    = "0.13.0"
    val flyway           = "7.8.2"
    val zioLogging       = "0.5.8"
    val log4j            = "2.14.1"
    val circe            = "0.13.0"
  }

  import Versions._

  val App =
    List(
      "com.github.pureconfig"        %% "pureconfig"          % pureConfig,
      "dev.zio"                      %% "zio-test-sbt"        % zio   % "test",
      "dev.zio"                      %% "zio-test"            % zio   % "test",
      "dev.zio"                      %% "zio"                 % zio,
      "org.apache.logging.log4j"      % "log4j-api"           % log4j,
      "org.apache.logging.log4j"      % "log4j-core"          % log4j,
      "org.apache.logging.log4j"      % "log4j-slf4j-impl"    % log4j,
      "ch.qos.logback"                % "logback-classic"     % "1.1.3",
      "io.circe"                     %% "circe-core"          % circe,
      "io.circe"                     %% "circe-generic"       % circe,
      "dev.zio"                      %% "zio-interop-cats"    % zioInteropCats,
      "dev.zio"                      %% "zio-logging-slf4j"   % zioLogging,
      "dev.zio"                      %% "zio-logging"         % zioLogging,
      "org.tpolecat"                 %% "doobie-core"         % doobie,
      "org.tpolecat"                 %% "doobie-h2"           % doobie,
      "org.tpolecat"                 %% "doobie-postgres"     % doobie,
      "org.tpolecat"                 %% "doobie-hikari"       % doobie,
      "org.http4s"                   %% "http4s-blaze-server" % http4s,
      "org.http4s"                   %% "http4s-circe"        % http4s,
      "org.http4s"                   %% "http4s-dsl"          % http4s,
      "io.github.kitlangton"         %% "zio-magic"           % "0.3.2",
      "org.fomkin"                   %% "korolev-http4s"      % "0.17.1",
      "org.fomkin"                   %% "korolev-zio"         % "0.17.1",
      "org.flywaydb"                  % "flyway-core"         % flyway,
      compilerPlugin(("org.typelevel" % "kind-projector"      % kindProjector).cross(CrossVersion.full))
    )
}
