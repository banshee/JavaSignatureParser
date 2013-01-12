// use sbt deliver-local to create ivy.xml

name := "JavaSignatureParser"

organization := "com.restphone"

version := "0.3-SNAPSHOT"

scalaVersion := "2.10.0-RC3"

publishMavenStyle := true

externalResolvers := Seq(
  "RESTPhone Nexus" at "http://git:8081/nexus/content/groups/public"
)

libraryDependencies ++= Seq(
  "org.scalaz" % "scalaz-core_2.10.0-RC3" % "7.0.0-M5",
  "com.google.guava" % "guava" % "13.0.1",
  "org.scalatest" % "scalatest_2.10.0-RC3" % "2.0.M5-B1" % "test"
)

transitiveClassifiers := Seq("sources")

publishTo <<= version { (v: String) =>
  val nexus = "http://git:8081/nexus/content/repositories/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "snapshots")
  else
    Some("releases" at nexus + "releases")
}

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")