name := "akka-assignment"

version := "0.1"

scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.12",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.12" % Test
)

libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.6.3" % Test

libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.0" % Test
