

name := "play-cxf-example"

version := "1.0-SNAPSHOT"

scalaVersion := "2.13.6"

val CxfVersion = "3.4.3"

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)

updateOptions := updateOptions.value.withLatestSnapshots(true)

libraryDependencies ++= Seq(
  guice,

  "org.apache.cxf" % "cxf-core"                 % CxfVersion,
  "org.apache.cxf" % "cxf-rt-frontend-jaxws"    % CxfVersion,
  "org.apache.cxf" % "cxf-rt-transports-http"   % CxfVersion,

  "eu.sipria.play" %% "play-guice-cxf_play28" % "1.8.0-RC1" /* changing() */,

  "org.scalatest"           %% "scalatest"          % "3.1.4"   % Test,
  "org.scalatestplus"       %% "junit-4-13"         % "3.1.4.0" % Test,
  "org.scalatestplus.play"  %% "scalatestplus-play" % "5.1.0"   % Test
)

CXF / version := CxfVersion

cxfDefaultArgs := Seq(
  "-p", "services"
)

cxfWSDLs := Seq(
  Wsdl("SunsetRiseService", (resourceDirectory in Compile).value / "HelloWorld.wsdl", Seq(
    "-wsdlLocation", "conf/HelloWorld.wsdl"
  ))
)

scalacOptions := Seq(
  "-deprecation"
)

lazy val root = Project("play-cxf-example", file("."))
  .enablePlugins(PlayScala)
  .enablePlugins(CxfPlugin)
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version),
    buildInfoPackage := "hello"
  )
