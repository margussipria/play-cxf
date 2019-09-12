package modules

import services.sunset.rise.SunSetRiseServiceSoap

class ClientModule extends org.apache.cxf.ClientModule {

  override def configure(): Unit = {
    bindClient[SunSetRiseServiceSoap]("services.sunset.rise.SunSetRiseServiceSoap")
  }
}
