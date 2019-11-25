package org.apache.cxf.config

import com.typesafe.config.{ConfigFactory, Config => TypesafeConfig}

class Configuration(realConfig: TypesafeConfig = ConfigFactory.load()) {

  def as[A: ConfigReader](path: String): A = {
    val reader = implicitly[ConfigReader[A]]
    reader.read(path, realConfig)
  }

  def asOption[A: ConfigReader](path: String): Option[A] = {
    if (realConfig.hasPath(path)) Some(as[A](path)) else None
  }
}

object Configuration {
  def apply(realConfig: TypesafeConfig = ConfigFactory.load()) = new Configuration(realConfig)
}
