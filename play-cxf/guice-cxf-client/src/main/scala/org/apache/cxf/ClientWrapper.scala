package org.apache.cxf

trait ClientWrapper extends ClientSetupHooks {
  def callback(value: AnyRef): Unit

  override def postCreate(value: AnyRef): Unit = callback(value)
}
