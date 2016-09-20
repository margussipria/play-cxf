import play.core.PlayVersion

val playVersionSuffix: String = {
  val versions = PlayVersion.current.split('.')
  require(versions.length >= 2)
  versions.take(2).mkString
}

name := s"play-guice-cxf_play$playVersionSuffix"

licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

organization := "eu.sipria.play"

version := "1.5.0"

scalaVersion := "2.11.8"

val CxfVersion = "3.1.7"

libraryDependencies += "com.typesafe.play" %% "play" % PlayVersion.current % Provided

libraryDependencies += "org.apache.cxf" % "cxf-core" % CxfVersion

libraryDependencies += "org.apache.cxf" % "cxf-rt-frontend-jaxws" % CxfVersion % Provided

libraryDependencies += "org.apache.cxf" % "cxf-rt-transports-http" % CxfVersion % Provided

scalacOptions := Seq(
  "-deprecation"
)

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

developers := List(
  Developer("kustra", "László Zsolt Kustra", "laszlo.kustra@gmail.com", url("http://laszlo.kustra.hu/")),
  Developer("margussipria", "Margus Sipria", "margus+play-cxf@sipria.fi", url("https://github.com/margussipria"))
)

scmInfo := Some(ScmInfo(
  url("https://github.com/margussipria/play-guice-cxf"),
  "scm:git:https://github.com/margussipria/play-guice-cxf.git",
  Some("scm:git:git@github.com:margussipria/play-guice-cxf.git")
))
