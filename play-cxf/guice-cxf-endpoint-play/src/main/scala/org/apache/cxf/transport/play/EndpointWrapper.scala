package org.apache.cxf.transport.play

import org.apache.cxf.jaxws.EndpointImpl

trait EndpointWrapper {
  def callback(value: EndpointImpl): Unit
}
