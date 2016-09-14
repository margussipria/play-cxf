package org.apache.cxf.config

import eri.commons.config.StringReader
import org.apache.cxf.binding.soap.{Soap11, Soap12, SoapVersion}

object ObjectReader  {

  implicit object SoapVersionReader extends StringReader[SoapVersion] {
    def apply(valueStr: String): SoapVersion = valueStr match {
      case "1.1" => Soap11.getInstance()
      case "1.2" => Soap12.getInstance()
      case _ => throw new Exception(s"SOAP version $valueStr is not supported")
    }
  }
}
