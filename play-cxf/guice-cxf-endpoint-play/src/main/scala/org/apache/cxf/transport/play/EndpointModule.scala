package org.apache.cxf.transport.play

import com.google.inject.name.Names
import com.google.inject.{Inject, Provider, Singleton}
import org.apache.cxf.config.{Configuration => CustomConfiguration}
import org.apache.cxf.jaxws.EndpointImpl
import org.apache.cxf.{Bus, CoreModule}
import play.api.{ConfigLoader, Configuration}

abstract class EndpointModule(eagerly: Boolean = true) extends CoreModule(eagerly) {
  import EndpointModule._

  protected def bindPlayTransport(): Unit = {
    bindBus()

    maybeEagerly {
      bind(classOf[PlayTransportFactory])
        .toProvider(classOf[PlayTransportFactoryProvider])
    }
  }

  protected def bindEndpoint(key: String, wrappers: Seq[Class[_ <: EndpointWrapper]] = Seq.empty): Unit = {
    bindPlayTransport()

    maybeEagerly {
      bind(classOf[EndpointImpl])
        .annotatedWith(Names.named(key))
        .toProvider(new EndpointImplProvider(key, wrappers))
    }
  }
}

object EndpointModule {
  final val EndpointKeyConfig = "apache.cxf.play.endpoint"

  @Singleton
  private class PlayTransportFactoryProvider @Inject() (
    bus: Bus,
    configuration: Configuration
  ) extends Provider[PlayTransportFactory] {
    def get(): PlayTransportFactory = {
      val factory = new PlayTransportFactory

      implicit val javaStringListLoader: ConfigLoader[java.util.List[String]] = ConfigLoader(_.getStringList)

      configuration.getOptional[java.util.List[String]]("apache.cxf.play.transports")
        .foreach(factory.setTransportIds)

      CoreModule.registerDestinationFactory(bus, factory)

      factory
    }
  }

  @Singleton
  class EndpointImplProvider(key: String, wrappers: Seq[Class[_ <: EndpointWrapper]] = Seq.empty) extends Provider[EndpointImpl] {
    @Inject var bus: Bus = _
    @Inject var injector: play.api.inject.Injector = _
    @Inject var configuration: Configuration = _

    def get(): EndpointImpl = {
      val config = configuration.getOptional[Configuration](EndpointKeyConfig)
        .flatMap(_.getOptional[Configuration](key))
        .getOrElse(Configuration.empty)

      val implementorClazz = Thread.currentThread().getContextClassLoader.loadClass(
        config.get[String]("implementor")
      )

      val endpoint = new EndpointImpl(bus, injector.instanceOf(implementorClazz))

      implicit val configWrapperLoader: ConfigLoader[CustomConfiguration] = {
        ConfigLoader(_.getConfig).map(CustomConfiguration.apply)
      }

      config.getOptional[CustomConfiguration]("bindingConfig")
        .map(CoreModule.createBindingConfig)
        .foreach(endpoint.setBindingConfig)

      wrappers.foreach { wrapperClass =>
        injector.instanceOf(wrapperClass).callback(endpoint)
      }

      endpoint.publish(config.get[String]("address"))

      endpoint
    }
  }
}
