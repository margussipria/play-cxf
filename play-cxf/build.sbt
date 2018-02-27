import play.core.PlayVersion

val playVersionSuffix: String = {
  val versions = PlayVersion.current.split('.')
  require(versions.length >= 2)
  versions.take(2).mkString
}

name := s"play-guice-cxf_play$playVersionSuffix"

homepage := Some(new URL("https://github.com/margussipria/play-guice-cxf"))
licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

organization := "eu.sipria.play"

version := "1.6.0"

scalaVersion := "2.11.11"

crossScalaVersions := Seq("2.11.11", "2.12.3")

val CxfVersion = "3.1.12"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play"         % PlayVersion.current % Provided,
  "com.typesafe.play" %% "play-guice"   % PlayVersion.current % Provided,

  "org.apache.cxf" % "cxf-core"                 % CxfVersion,
  "org.apache.cxf" % "cxf-rt-frontend-jaxws"    % CxfVersion % Provided,
  "org.apache.cxf" % "cxf-rt-transports-http"   % CxfVersion % Provided
)

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
