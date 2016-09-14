import play.core.PlayVersion

name := "play-cxf-example"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.8"

val playVersionSuffix: String = {
  val versions = PlayVersion.current.split('.')
  require(versions.length >= 2)
  versions.take(2).mkString
}

val CxfVersion = "3.1.7"

libraryDependencies += "org.apache.cxf" % "cxf-rt-bindings-soap" % CxfVersion

libraryDependencies += "org.apache.cxf" % "cxf-rt-frontend-jaxws" % CxfVersion

libraryDependencies += "org.apache.cxf" % "cxf-rt-transports-http" % CxfVersion

libraryDependencies += "eu.imind.play" %% "play-cxf_play25" % "1.5.0-SNAPSHOT"

version in cxf := CxfVersion

defaultArgs in wsdl2java := Seq(
  "-p", "services.sunset.rise"
)

wsdls in wsdl2java := Seq(
  Wsdl("SunsetRiseService", (resourceDirectory in Compile).value / "sunsetriseservice.wsdl", Seq(
    "-wsdlLocation", "conf/sunsetriseservice.wsdl"
  ))
)

scalacOptions := Seq(
  "-deprecation"
)

lazy val playCxf = RootProject(file("../../play-cxf/"))

lazy val root = Project("play-cxf-example", file("."))
  .enablePlugins(PlayScala)
  .enablePlugins(CxfPlugin)
  .dependsOn(playCxf)
