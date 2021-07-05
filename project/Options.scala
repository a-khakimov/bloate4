import sbt._

object Options {

  def scalacOptions(scalaVersion: String, optimize: Boolean): Seq[String] = {
    val baseOptions = Seq(
      "-deprecation",
      "-feature",
      "-Ymacro-annotations",
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
