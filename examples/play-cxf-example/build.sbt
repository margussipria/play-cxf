

name := "play-cxf-example"

version := "1.0-SNAPSHOT"

scalaVersion := "2.13.1"

val CxfVersion = "3.3.3"

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)

updateOptions := updateOptions.value.withLatestSnapshots(true)

libraryDependencies ++= Seq(
  guice,

  "org.apache.cxf" % "cxf-core"                 % CxfVersion,
  "org.apache.cxf" % "cxf-rt-frontend-jaxws"    % CxfVersion,
  "org.apache.cxf" % "cxf-rt-transports-http"   % CxfVersion,

  "eu.sipria.play" %% "play-guice-cxf_play27" % "1.7.0-SNAPSHOT" changing(),

  "org.scalatest"           %% "scalatest"          % "3.0.8" % Test,
  "org.scalatestplus.play"  %% "scalatestplus-play" % "4.0.3" % Test
)

version in CXF := CxfVersion

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
