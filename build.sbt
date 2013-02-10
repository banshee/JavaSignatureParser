// use sbt deliver-local to create ivy.xml

name := "JavaSignatureParser"

organization := "com.restphone"

version := "0.4-SNAPSHOT"

scalaVersion := "2.10.0"

publishMavenStyle := true

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.0.0-M7",
  "com.google.guava" % "guava" % "13.0.1",
  "org.scalatest" %% "scalatest" % "2.0.M5b" % "test"
)
