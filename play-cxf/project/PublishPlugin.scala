import sbt.Keys._
import sbt._

object PublishPlugin extends sbt.AutoPlugin {
  override def requires: Plugins = empty
  override def trigger: PluginTrigger = allRequirements
  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },

    publishMavenStyle := true,

    publishArtifact in Test := false,

    pomIncludeRepository := { _ => false }
  )
}
