package org.apache.cxf

import java.io.File
import java.time.{Clock, Instant, ZoneOffset}

import com.google.inject.AbstractModule
import org.apache.cxf.transport.play.EndpointModule
import org.apache.date_and_time_soap_http.{AskTimeRequest, DateAndTime}
import org.scalatest.{FreeSpec, Matchers}
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModuleConversions}
import play.api.test.{Helpers, TestServer}
import play.api.{Application, Configuration, Environment}

import scala.language.implicitConversions

class ServiceSpec extends FreeSpec with GuiceableModuleConversions with Matchers {

  type Builder = GuiceApplicationBuilder

  private def withApplication(f: Builder => Builder = builder => builder)(testCode: Application => Any): Any = {

    class DateAndTimeServiceModule extends EndpointModule {
      def configure(): Unit = {
        bindEndpoint("DateAndTimeService", Seq())
      }
    }

    class DateAndTimeClientModule extends ClientModule {
      def configure(): Unit = {
        bindClient[DateAndTime]("org.apache.date_and_time_soap_http.DateAndTime", Seq())
      }
    }

    val builder = new GuiceApplicationBuilder()
      .bindings(
        fromGuiceModule(new AbstractModule {
          override def configure(): Unit = {
            bind(classOf[Clock]).toInstance(Clock.systemUTC())

            install(new DateAndTimeServiceModule)
            install(new DateAndTimeClientModule)
          }
        })
      )

      .appRoutes( app => {
        case ("GET",  "/service") => app.injector.instanceOf[org.apache.cxf.transport.play.CxfController].handle()
        case ("POST", "/service") => app.injector.instanceOf[org.apache.cxf.transport.play.CxfController].handle()
      })
      .loadConfig(
        Configuration.load(Environment.simple(new File(getClass.getResource("/application.conf").getPath)))
      )


    val application = f(builder).build()

    Helpers.running(TestServer(Helpers.testServerPort, application)) {
      testCode(application)
    }
  }

  "A time server" - {
    "should return correct time" in withApplication { builder =>
      builder.overrides(
        fromGuiceModule(new AbstractModule {
          override def configure(): Unit = {
            bind(classOf[Clock]).toInstance(
              Clock.fixed(Instant.parse("2013-10-27T22:04:00.246Z"), ZoneOffset.UTC)
            )
          }
        })
      )
    } { app =>

      val request = new AskTimeRequest()
      request.setTimeZone("Europe/Helsinki")

      app.injector.instanceOf[DateAndTime]
        .askTime(request).getResponse.toString should be ("2013-10-28T00:04:00.246")
    }

    "should respond to multiple requests with correct time" in withApplication { builder =>
      builder.overrides(
        fromGuiceModule(new AbstractModule {
          override def configure(): Unit = {
            bind(classOf[Clock]).toInstance(
              Clock.fixed(Instant.parse("2013-10-27T22:04:00.000Z"), ZoneOffset.UTC)
            )
          }
        })
      )
    } { app =>

      val request = new AskTimeRequest()
      request.setTimeZone("Europe/Helsinki")

      val service = app.injector.instanceOf[DateAndTime]

      (1 to 25).par.map { _ => service.askTime(request) } foreach { response =>
        response.getResponse.toString should be ("2013-10-28T00:04:00.000")
      }
    }
  }
}
