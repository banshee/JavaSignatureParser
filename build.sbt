// use sbt deliver-local to create ivy.xml

// "com.restphone" % "javasignatureparser" % "0.7"

name := "JavaSignatureParser"

organization := "com.restphone"

version := "0.7"

scalaVersion := "2.11.0"

publishMavenStyle := true

publishArtifact in Test := false

libraryDependencies ++= Seq(
  "com.google.guava" % "guava" % "16.0.1",
  "com.restphone" %% "scalatestutilities" % "0.6" % "test",
  "org.scalaz" %% "scalaz-core" % "7.0.6"
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

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}
