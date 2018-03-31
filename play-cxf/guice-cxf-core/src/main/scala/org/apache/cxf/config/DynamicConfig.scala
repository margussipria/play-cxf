package org.apache.cxf.config

import com.typesafe.config.{ConfigException, ConfigFactory, Config => TypesafeConfig}

import scala.language.dynamics

class DynamicConfig(realPath: String = "", realConfig: TypesafeConfig = ConfigFactory.load()) extends Dynamic {
  def this(config: TypesafeConfig) = this("", config)

  @throws[ConfigException.Generic]("Type parameter not specified or not supported")
  def as[A: ConfigReader]: A = {
    val reader = implicitly[ConfigReader[A]]
    reader(realPath, realConfig)
  }

  def asOption[A: ConfigReader]: Option[A] = {
    if (realConfig.hasPath(realPath)) Some(as[A]) else None
  }

  /** Traversal magic as supported by `scala.Dynamic`. */
  def selectDynamic(name: String): DynamicConfig = {
    val next = if (realPath.nonEmpty && realConfig.hasPath(realPath)) {
      realConfig.getConfig(realPath)
    } else  {
      realConfig
    }
    new DynamicConfig(name, next)
  }
}
