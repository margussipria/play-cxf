// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository
resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.8")

addSbtPlugin("io.paymenthighway.sbt" % "sbt-cxf" % "1.7")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.10.0")
