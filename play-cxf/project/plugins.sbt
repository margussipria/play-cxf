// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository 
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

def propOr(name: String, value: String): String = {
  (sys.props get name) orElse (sys.env get name) getOrElse value
}

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % propOr("play.version", "2.6.12"))

addSbtPlugin("io.paymenthighway.sbt" % "sbt-cxf" % "1.4")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")

addSbtPlugin("com.github.sbt" % "sbt-jacoco" % "3.0.3")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.1")
