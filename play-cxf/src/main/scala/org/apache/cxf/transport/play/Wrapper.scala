package org.apache.cxf.transport.play

/**
  * Created by margus on 14.9.2016.
  */
trait Wrapper {
  def callback(value: AnyRef): Unit
}
