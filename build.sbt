// use sbt deliver-local to create ivy.xml

name := "JavaSignatureParser"

organization := "com.restphone"

version := "0.6"

scalaVersion := "2.10.2"

publishMavenStyle := true

publishArtifact in Test := false

libraryDependencies ++= Seq(
  "org.scalaz" % "scalaz-core_2.10" % "7.0.2",
  "com.google.guava" % "guava" % "14.0.1",
  "org.scalatest" %% "scalatest" % "2.0.M5b" % "test"
)

pomExtra := (
  <url>https://github.com/banshee/JavaSignatureParser</url>
  <licenses>
    <license>
      <name>GPL</name>
      <url>https://github.com/banshee/JavaSignatureParser/blob/master/COPYING</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git:git@github.com:banshee/JavaSignatureParser.git</url>
    <connection>scm:git:git@github.com:banshee/JavaSignatureParser.git</connection>
  </scm>
  <developers>
    <developer>
      <id>jamesmoore</id>
      <name>James Moore</name>
      <organization>RESTPhone</organization>
      <organizationUrl>http://restphone.com</organizationUrl>
    </developer>
  </developers>)
