package modules

class EndpointModule extends org.apache.cxf.transport.play.EndpointModule {

  override def configure(): Unit = {
    bindEndpoint("helloWorld")
  }
}
