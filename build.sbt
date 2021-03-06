version := "0.1"

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
    scalaVersion := "2.13.6"
  )
)

lazy val root = (project in file("."))
  .settings(
    name := "bloate4",
    libraryDependencies ++= Dependencies.App,
    Compile / scalacOptions := Options.scalacOptions(scalaVersion.value, isSnapshot.value)
  )

sonarProperties := Sonar.properties
