// use sbt deliver-local to create ivy.xml

name := "JavaSignatureParser"

organization := "com.restphone"

version := "0.4-SNAPSHOT"

scalaVersion := "2.10.0"

publishMavenStyle := true

libraryDependencies ++= Seq(
  "com.google.guava" % "guava" % "13.0.1",
  "org.scalaz" %% "scalaz-core" % "latest.snapshot",
  "org.scalatest" %% "scalatest" % "latest.snapshot" % "test",
  "org.scala-lang" % "scala-actors" % "2.10.0"
)
