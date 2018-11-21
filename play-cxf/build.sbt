def playVersionSuffix: String = {
  val versions = PlayVersion.split('.')
  require(versions.length >= 2)
  versions.take(2).mkString
}

val CxfVersion = "3.2.7"
val PlayVersion = play.core.PlayVersion.current

enablePlugins(JacocoPlugin)

version in CXF := CxfVersion

def module(id: String, base: java.io.File): Project = {
  Project(id, base)
    .settings(
      homepage := Some(new URL("https://github.com/margussipria/play-guice-cxf")),
      licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),

      organization := "eu.sipria.play",

      version := "1.6.3-SNAPSHOT",

      scalaVersion := "2.12.7",
      crossScalaVersions := Seq("2.11.12", "2.12.7"),

      scalacOptions := Seq(
        "-deprecation"
      ),

      testOptions in Test ++= Seq(
        Tests.Argument("-oDF"),
        Tests.Argument(TestFrameworks.ScalaTest, "-u", "%s" format ((target in Test).value / "test-reports"))
      ),

      fork in Test := true
    )
}

val guiceCore = module("guice-cxf-core", file("guice-cxf-core"))
  .settings(
    libraryDependencies ++= Seq(
      "org.apache.cxf" % "cxf-core"               % CxfVersion,
      "org.apache.cxf" % "cxf-rt-frontend-jaxws"  % CxfVersion  % Provided,

      "com.google.inject" % "guice"               % "4.2.2",

      "com.typesafe.play" %% "play"               % PlayVersion % Optional,
    )
  )

val guiceClient = module("guice-cxf-client", file("guice-cxf-client"))
  .settings(
    libraryDependencies ++= Seq(
      "org.apache.cxf" % "cxf-rt-transports-http" % CxfVersion  % Provided,
      "org.apache.cxf" % "cxf-rt-frontend-jaxws"  % CxfVersion  % Provided,

      "com.typesafe" % "config"                   % "1.3.3"
    )
  )
  .dependsOn(guiceCore)
  .aggregate(guiceCore)

val guicePlayEndpoint = module("guice-cxf-endpoint-play", file("guice-cxf-endpoint-play"))
  .enablePlugins(CxfPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play"                 % PlayVersion % Provided,
      "com.typesafe.play" %% "play-guice"           % PlayVersion % Provided,

      "org.apache.cxf" % "cxf-core"                 % CxfVersion,
      "org.apache.cxf" % "cxf-rt-frontend-jaxws"    % CxfVersion  % Provided,

      "com.typesafe.play" %% "play"                 % PlayVersion % Test,
      "com.typesafe.play" %% "play-guice"           % PlayVersion % Test,

      "org.apache.cxf" % "cxf-rt-transports-http"   % CxfVersion  % Test,

      "org.scalatest"           %% "scalatest"          % "3.0.5" % Test,
      "org.scalatestplus.play"  %% "scalatestplus-play" % "3.1.2" % Test
    ),

    CXF / version := CxfVersion,

    Test / cxfDefaultArgs := Seq(
      "-exsh", "true",
      "-validate",
      "-wv", "1.1"
    ),

    Test / cxfWSDLs := Seq(
      Wsdl("DateAndTime", (resourceDirectory in Test).value / "wsdl/DateAndTime.wsdl", Seq(
        "-wsdlLocation", "classpath:wsdl/DateAndTime.wsdl"
      ))
    )
  )
  .dependsOn(guiceCore, guiceClient % "test->test")
  .aggregate(guiceCore)

val root = module("root", file("."))
  .settings(
    // for publishing "legacy" style named artifact (combining client + endpoint)
    name := s"play-guice-cxf_play$playVersionSuffix"
  )
  .dependsOn(guiceClient, guicePlayEndpoint)
  .aggregate(guiceClient, guicePlayEndpoint)
