package org.apache.cxf

trait Wrapper {
  def callback(value: AnyRef): Unit
}
