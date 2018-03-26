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

version := "1.6.1-SNAPSHOT"

scalaVersion := "2.12.5"

crossScalaVersions := Seq("2.11.12", "2.12.5")

val CxfVersion = "3.2.3"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play"         % PlayVersion.current % Provided,
  "com.typesafe.play" %% "play-guice"   % PlayVersion.current % Provided,

  "com.typesafe.play" %% "play"         % PlayVersion.current % Test,
  "com.typesafe.play" %% "play-guice"   % PlayVersion.current % Test,

  "org.apache.cxf" % "cxf-core"                 % CxfVersion,
  "org.apache.cxf" % "cxf-rt-frontend-jaxws"    % CxfVersion  % Provided,
  "org.apache.cxf" % "cxf-rt-transports-http"   % CxfVersion  % Provided,

  "org.scalatest"           %% "scalatest"          % "3.0.5" % Test,
  "org.scalatestplus.play"  %% "scalatestplus-play" % "3.1.2" % Test
)

testOptions in Test ++= Seq(
  Tests.Argument("-oDF"),
  Tests.Argument(TestFrameworks.ScalaTest, "-u", "%s" format ((target in Test).value / "test-reports"))
)

scalacOptions := Seq(
  "-deprecation"
)

enablePlugins(JacocoPlugin)
enablePlugins(CxfPlugin)

version in CXF := CxfVersion

defaultArgs in test in wsdl2java := Seq(
  "-exsh", "true",
  "-validate",
  "-wv", "1.1"
)

wsdls in test in wsdl2java := Seq(
  Wsdl("DateAndTime", (resourceDirectory in Test).value / "wsdl/DateAndTime.wsdl", Seq(
    "-wsdlLocation", "classpath:wsdl/DateAndTime.wsdl"
  ))
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
