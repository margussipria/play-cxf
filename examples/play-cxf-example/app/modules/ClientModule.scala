package modules

import services.Greeter

class ClientModule extends org.apache.cxf.ClientModule {

  override def configure(): Unit = {
    bindClient[Greeter]("services.hello.Greeter")
  }
}
