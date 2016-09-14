package org.apache.cxf

trait ClientWrapper {
  def callback(value: AnyRef): Unit
}
