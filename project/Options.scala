import sbt._

object Options {

  def scalacOptions(scalaVersion: String, optimize: Boolean): Seq[String] = {
    val baseOptions = Seq(
      "-deprecation",
      "-feature",
      "-Ymacro-annotations",
      "-Xfatal-warnings",
      "-Ywarn-value-discard",
      "-Ywarn-numeric-widen",
      "-Ywarn-extra-implicit",
      "-Ywarn-unused",
      "-Xsource:3"
    )

    val optimizeOptions =
      if (optimize) {
        Seq(
          "-opt:l:inline"
        )
      } else Seq.empty

    baseOptions ++ optimizeOptions
  }
}
