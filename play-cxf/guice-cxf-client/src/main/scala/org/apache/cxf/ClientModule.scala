package org.apache.cxf

import com.google.inject.{Inject, Provider, Singleton}
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.cxf.binding.soap.{SoapBindingConfiguration, SoapVersion}
import org.apache.cxf.config.DynamicConfig
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean
import org.apache.cxf.transport.http.HTTPTransportFactory

import scala.reflect.{ClassTag, _}

abstract class ClientModule(eagerly: Boolean = true) extends CoreModule(eagerly) {
  import ClientModule._

  protected def bindHTTPTransport(): Unit = {
    bindBus()

    maybeEagerly {
      bind(classOf[HTTPTransportFactory])
        .toProvider(classOf[HTTPTransportFactoryProvider])
    }
  }

  protected def bindClient[T : ClassTag](key: String, wrappers: Seq[Class[_ <: ClientWrapper]] = Seq.empty): Unit = {
    bindHTTPTransport()

    maybeEagerly {
      bind(classTag[T].runtimeClass.asInstanceOf[Class[T]])
        .toProvider(new ClientProvider[T](key, wrappers))
    }
  }
}

object ClientModule {
  final val ClientKeyConfig = "apache.cxf.client"

  @Singleton
  private class HTTPTransportFactoryProvider @Inject() (bus: Bus) extends Provider[HTTPTransportFactory] {

    def get(): HTTPTransportFactory = {
      val factory = new HTTPTransportFactory

      CoreModule.registerDestinationFactory(bus, factory)

      factory
    }
  }

  class ClientProvider[T : ClassTag](key: String, wrappers: Seq[Class[_ <: ClientWrapper]] = Seq.empty) extends javax.inject.Provider[T] {
    @Inject var bus: Bus = _
    @Inject var injector: com.google.inject.Injector = _
    var config: Config = ConfigFactory.load()

    @Inject(optional = true)
    def setConfig(config: Config): Unit = {
      this.config = config
    }

    override def get(): T = {
      val config = this.config.getConfig(ClientKeyConfig)

      val factory = new JaxWsProxyFactoryBean()
      factory.setBus(bus)
      factory.setServiceClass(classTag[T].runtimeClass.asInstanceOf[Class[T]])

      Option(config.getObject(key)).foreach { config =>
        val dynamicConfig = new DynamicConfig(config.toConfig)
        dynamicConfig.address.asOption[String].foreach(factory.setAddress)

        dynamicConfig.bindingConfig.asOption[Config].map(new DynamicConfig(_)).map { config =>
          val bindingConfig = new SoapBindingConfiguration

          config.version.asOption[SoapVersion].foreach(bindingConfig.setVersion)

          bindingConfig
        }.foreach(factory.setBindingConfig)
      }

      val service = factory.create()

      wrappers.foreach { wrapperClass =>
        injector.getInstance(wrapperClass).callback(service)
      }

      service.asInstanceOf[T]
    }
  }
}
