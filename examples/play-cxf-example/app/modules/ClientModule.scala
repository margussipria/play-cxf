package modules

import services.sunset.rise.SunSetRiseServiceSoap

class ClientModule extends org.apache.cxf.ClientModule {

  def configure(): Unit = {
    bindClient[SunSetRiseServiceSoap]("services.sunset.rise.SunSetRiseServiceSoap")
  }
}
