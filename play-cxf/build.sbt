import play.core.PlayVersion

val playVersionSuffix: String = {
  val versions = PlayVersion.current.split('.')
  require(versions.length >= 2)
  versions.take(2).mkString
}

name := s"play-cxf_play$playVersionSuffix"

homepage := Some(url("http://www.imind.eu/web/2013/11/07/developing-soap-services-using-play-framework-2-2-x/"))
licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

organization := "eu.imind.play"
organizationName := "iMind"
organizationHomepage := Some(url("http://imind.eu/"))

version := "1.5.0-SNAPSHOT"

scalaVersion := "2.11.8"

resolvers += "ERI OSS" at "http://dl.bintray.com/elderresearch/OSS"

val CxfVersion = "3.1.7"

libraryDependencies += "com.typesafe.play" %% "play" % PlayVersion.current % Provided

libraryDependencies += "org.apache.cxf" % "cxf-core" % CxfVersion

libraryDependencies += "org.apache.cxf" % "cxf-rt-frontend-jaxws" % CxfVersion % Provided

libraryDependencies += "org.apache.cxf" % "cxf-rt-transports-http" % CxfVersion % Provided

libraryDependencies += "com.elderresearch" %% "ssc" % "0.2.0"

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
  url("https://github.com/imindeu/play-cxf"),
  "scm:git:https://github.com/imindeu/play-cxf.git",
  Some("scm:git:git@github.com:imindeu/play-cxf.git")
))
