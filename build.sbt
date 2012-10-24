name := "JavaSignatureParser"

organization := "com.restphone"

version := "0.2"

scalaVersion := "2.10.0-SNAPSHOT"

// Compile for these Scala versions
crossScalaVersions := Seq("2.9.1", "2.9.2", "2.10.0-RC1")

publishMavenStyle := true

externalResolvers := Seq(
  "RESTPhone Nexus" at "http://git:8081/nexus/content/groups/public"
)

libraryDependencies ++= Seq(
  "com.restphone" % "artifactname" % "1.0",
  "org.scalaz" % "scalaz-concurrent_2.10.0-M7" % "7.0.0-M3"
)

publishTo <<= version { (v: String) =>
  val nexus = "http://git:8081/nexus/content/repositories/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "snapshots")
  else
    Some("releases" at nexus + "releases")
}

credentials += Credentials("Sonatype Nexus Repository Manager", 
                           "git", 
                           "deployment",
                           "deploy")
