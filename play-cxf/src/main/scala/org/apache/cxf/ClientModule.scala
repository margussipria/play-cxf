package org.apache.cxf

import javax.inject.{Inject, Provider, Singleton}

import com.typesafe.config.{Config, ConfigFactory}
import eri.commons.config.SSConfig
import org.apache.cxf.binding.soap.{SoapBindingConfiguration, SoapVersion}
import org.apache.cxf.config.ObjectReader._
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean
import org.apache.cxf.transport.DestinationFactoryManager
import org.apache.cxf.transport.http.HTTPTransportFactory

import scala.collection.JavaConverters._
import scala.reflect.{ClassTag, _}

abstract class ClientModule extends CoreModule {
  import ClientModule._

  protected def bindHTTPTransport(): Unit = {
    bindBus()

    bind(classOf[HTTPTransportFactory])
      .toProvider(classOf[HTTPTransportFactoryProvider])
      .asEagerSingleton()
  }

  protected def bindClient[T : ClassTag](key: String, wrappers: Seq[Class[_ <: ClientWrapper]] = Seq.empty): Unit = {
    bindHTTPTransport()

    bind(classTag[T].runtimeClass.asInstanceOf[Class[T]])
      .toProvider(new ClientProvider[T](key, wrappers))
      .asEagerSingleton()
  }
}

object ClientModule {
  final val ClientKeyConfig = "apache.cxf.client"

  @Singleton
  private class HTTPTransportFactoryProvider @Inject() (bus: Bus) extends Provider[HTTPTransportFactory] {

    def get(): HTTPTransportFactory = {
      val factory = new HTTPTransportFactory

      val dfm = bus.getExtension(classOf[DestinationFactoryManager])

      factory.getTransportIds.asScala.foreach(dfm.registerDestinationFactory(_, factory))

      factory
    }
  }

  class ClientProvider[T : ClassTag](key: String, wrappers: Seq[Class[_ <: ClientWrapper]] = Seq.empty) extends javax.inject.Provider[T] {
    @Inject var bus: Bus = _
    @Inject var injector: com.google.inject.Injector = _

    override def get(): T = {
      val config = ConfigFactory.load().getConfig(ClientKeyConfig)

      val factory = new JaxWsProxyFactoryBean()
      factory.setBus(bus)
      factory.setServiceClass(classTag[T].runtimeClass.asInstanceOf[Class[T]])

      Option(config.getObject(key)).foreach { config =>
        val dynamicConfig = new SSConfig(config.toConfig)
        dynamicConfig.address.asOption[String].foreach(factory.setAddress)

        dynamicConfig.bindingConfig.asOption[Config].map(new SSConfig(_)).map { config =>
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
