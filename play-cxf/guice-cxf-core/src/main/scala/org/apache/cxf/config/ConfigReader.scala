package org.apache.cxf.config

import com.typesafe.config.{Config, ConfigValue}
import org.apache.cxf.binding.soap.{Soap11, Soap12, SoapVersion}

import scala.collection.compat._
import scala.jdk.CollectionConverters._
import scala.language.higherKinds

trait ConfigReader[T] { self =>
  private final val DummyPath = "dummy"

  def read(path: String, config: Config): T

  def read(configValue: ConfigValue): T = {
    read(DummyPath, configValue.atPath(DummyPath))
  }

  def map[B](f: T => B): ConfigReader[B] = new ConfigReader[B] {
    def read(path: String, config: Config): B = f(self.read(path, config))
  }
}

object ConfigReader {
  def apply[T](f: Config => String => T): ConfigReader[T] = new ConfigReader[T] {
    def read(path: String, config: Config): T = f(config)(path)
  }

  implicit val stringReader: ConfigReader[String] = ConfigReader(_.getString)
  implicit val configReader: ConfigReader[Config] = ConfigReader(_.getConfig)

  implicit val configurationReader: ConfigReader[Configuration] = configReader.map(Configuration.apply)

  implicit val soapVersionReader: ConfigReader[SoapVersion] = ConfigReader(_.getString).map {
    case "1.1" => Soap11.getInstance()
    case "1.2" => Soap12.getInstance()
    case value => throw new Exception(s"SOAP version $value is not supported")
  }

  implicit def collectionConfigReader[C[_], T: ConfigReader](
    implicit reader: ConfigReader[T], factory: Factory[T, C[T]]
  ): ConfigReader[C[T]] = {
    ConfigReader(_.getList)
      .map {
        _
          .asScala
          .map(reader.read)
          .to(factory)
      }
  }

  implicit def mapConfigReader[T: ConfigReader](implicit reader: ConfigReader[T]): ConfigReader[Map[String, T]] = {
    ConfigReader(_.getConfig)
      .map {
        _
          .entrySet()
          .asScala
          .map { entry =>
            entry.getKey -> reader.read(entry.getValue)
          }
          .toMap
      }
  }
}
