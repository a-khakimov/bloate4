version := "0.1"

scalaVersion := "2.13.6"

idePackagePrefix := Some("org.github.ainr.bloate4")

inThisBuild(
  List(
    organization := "com.ainr",
    developers := List(
      Developer(
        "ainr",
        "Ainur Khakimov",
        "",
        url("https://github.com/a-khakimov")
      )
    ),
    semanticdbEnabled := true,
    scalaVersion := "2.13.4"
  )
)

lazy val root = (project in file("."))
  .settings(
    name := "bloate4",
    libraryDependencies ++= Dependencies.App,
    scalacOptions in ThisBuild := Options.scalacOptions(scalaVersion.value, isSnapshot.value)
  )