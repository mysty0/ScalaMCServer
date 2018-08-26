name := "ScalaMCServer"

version := "1.0"

scalaVersion := "2.12.1"

scalacOptions ++= Seq(
  "-feature"
)

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies +=
  "com.typesafe.akka" %% "akka-actor" % "2.4.19"
val circeVersion = "0.8.0"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

resolvers += Resolver.sonatypeRepo("releases")
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.4"
libraryDependencies += "org.scalatest" % "scalatest_2.12" % "3.0.4" % "test"

libraryDependencies += "org.clapper" %% "classutil" % "1.1.2"

libraryDependencies += "org.reflections" % "reflections" % "0.9.10"

lazy val root = (project in file(".")).
  settings(
    name := "ScalaMC",
    version := "1.0",
    scalaVersion := "2.11.4",
    mainClass in Compile := Some("com.scalamc.ScalaMC")
  )