package org.apache.cxf.config

import com.typesafe.config.{ConfigFactory, Config => TypesafeConfig}

class Configuration(realConfig: TypesafeConfig = ConfigFactory.load()) {

  def as[A: ConfigReader](path: String)(implicit reader: ConfigReader[A]): A = {
    reader.read(realConfig, path)
  }

  def asOption[A: ConfigReader](path: String): Option[A] = {
    if (realConfig.hasPath(path)) Some(as[A](path)) else None
  }
}

object Configuration {
  def apply(realConfig: TypesafeConfig = ConfigFactory.load()) = new Configuration(realConfig)
}
