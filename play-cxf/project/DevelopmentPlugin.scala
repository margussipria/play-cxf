import sbt.Keys._
import sbt._

object DevelopmentPlugin extends sbt.AutoPlugin {
  override def requires: Plugins = empty
  override def trigger: PluginTrigger = allRequirements
  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    developers := List(
      Developer("kustra", "László Zsolt Kustra", "laszlo.kustra@gmail.com", url("http://laszlo.kustra.hu/")),
      Developer("margussipria", "Margus Sipria", "margus+play-cxf@sipria.fi", url("https://github.com/margussipria"))
    ),

    scmInfo := Some(ScmInfo(
      url("https://github.com/margussipria/play-guice-cxf"),
      "scm:git:https://github.com/margussipria/play-guice-cxf.git",
      Some("scm:git:git@github.com:margussipria/play-guice-cxf.git")
    ))
  )
}
