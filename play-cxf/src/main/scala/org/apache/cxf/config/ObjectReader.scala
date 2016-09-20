package org.apache.cxf.config

import com.typesafe.config.Config
import org.apache.cxf.binding.soap.{Soap11, Soap12, SoapVersion}

import scala.collection.JavaConversions._

trait ConfigReader[T] {
  def apply(path: String, config: Config): T
}

trait StringReader[T] {
  def apply(valueStr: String): T
}

object StringReader {
  implicit object SoapVersionReader extends StringReader[SoapVersion] {
    def apply(valueStr: String): SoapVersion = valueStr match {
      case "1.1" => Soap11.getInstance()
      case "1.2" => Soap12.getInstance()
      case _ => throw new Exception(s"SOAP version $valueStr is not supported")
    }
  }
}

object ConfigReader  {
  implicit object StringReader extends ConfigReader[String] {
    def apply(path: String, config: Config): String = config.getString(path)
  }
  implicit object StringSeqReader extends ConfigReader[Seq[String]] {
    def apply(path: String, config: Config): Seq[String] =
      config.getStringList(path)
  }

  implicit object ConfigReader extends ConfigReader[Config] {
    def apply(path: String, config: Config): Config = config.getConfig(path)
  }

  /** Given a `StringReader[T]`, creates a `ConfigReader[T]` */
  implicit def customConfigReader[T: StringReader]: ConfigReader[T] = new ConfigReader[T] {
    override def apply(path: String, config: Config): T = {
      val reader = implicitly[StringReader[T]]
      reader(config.getString(path))
    }
  }

  /** Given a `StringReader[T]`, creates a `ConfigReader[Seq[T]]` */
  implicit def customConfigSeqReader[T: StringReader]: ConfigReader[Seq[T]] = new ConfigReader[Seq[T]] {
    def apply(path: String, config: Config): Seq[T] = {
      val reader = implicitly[StringReader[T]]
      config.getStringList(path).map(reader.apply)
    }
  }
}
