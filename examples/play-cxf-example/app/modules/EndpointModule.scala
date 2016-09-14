package modules

class EndpointModule extends org.apache.cxf.transport.play.EndpointModule {

  def configure(): Unit = {
    bindEndpoint("helloWorld")
  }
}
