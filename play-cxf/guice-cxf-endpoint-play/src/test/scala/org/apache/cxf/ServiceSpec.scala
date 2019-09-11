package org.apache.cxf

import java.io.File
import java.time.{Clock, Instant, ZoneId, ZoneOffset}
import java.util.concurrent.ConcurrentLinkedQueue

import javax.inject.Provider

import com.google.inject.AbstractModule
import org.apache.cxf.message.Message
import org.apache.cxf.phase.{AbstractPhaseInterceptor, Phase}
import org.apache.cxf.transport.play.EndpointModule
import org.apache.date_and_time_soap_http.{AskTimeRequest, DateAndTime}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, Matchers}
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModuleConversions}
import play.api.test.{Helpers, TestServer}
import play.api.{Application, Configuration, Environment}

import scala.language.implicitConversions

class ServiceSpec extends FreeSpec with GuiceableModuleConversions with Matchers with ScalaFutures {

  type Builder = GuiceApplicationBuilder

  class DateAndTimeServiceModule extends EndpointModule(false) {

    override def configure(): Unit = {
      bindEndpoint("DateAndTimeService", Seq.empty)
    }
  }

  class DateAndTimeClientModule extends ClientModule(false) {
    override def configure(): Unit = {
      bindClient[DateAndTime]("org.apache.date_and_time_soap_http.DateAndTime", Seq.empty)
    }
  }

  private def withApplication(f: Builder => Builder = identity)(testCode: Application => Any): Any = {

    val builder = GuiceApplicationBuilder(eagerly = true)
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
        case ("GET" | "POST", "/service") =>
          app.injector.instanceOf[org.apache.cxf.transport.play.CxfController].handle()
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
      val queue = interceptResponseCode(service)

      (1 to 25).par.map { _ => service.askTime(request) } foreach { response =>
        response.getResponse.toString should be ("2013-10-28T00:04:00.000")
      }

      queue should have size 25
      queue should contain only "200"
    }

    "should return fault with correct http status" in withApplication { builder =>
      builder.overrides(
        fromGuiceModule(new AbstractModule {
          override def configure(): Unit = {
            bind(classOf[Clock]).toProvider(new Provider[Clock] {
              def get: Clock = new Clock {
                override def getZone: ZoneId = throw new Exception("Not Implemented")
                override def withZone(zone: ZoneId): Clock = throw new Exception("No Clock")
                override def instant(): Instant = throw new Exception("Not Implemented")
              }
            })
          }
        })
      )
    } { app =>
      val service = app.injector.instanceOf[DateAndTime]

      val request = new AskTimeRequest()
      request.setTimeZone("Europe/Helsinki")

      val queue = interceptResponseCode(service)

      val fault = intercept[org.apache.date_and_time_soap_http.AskTimeFault] {
        service.askTime(request)
      }

      fault.getFaultInfo.getErrorCode should be ("667")
      fault.getFaultInfo.getErrorMessage should be ("Test Error Message")

      queue should have size 1
      queue should contain ("500")
    }
  }

  def interceptResponseCode(service: AnyRef): ConcurrentLinkedQueue[String] = {
    val queue = new ConcurrentLinkedQueue[String]

    val client = org.apache.cxf.frontend.ClientProxy.getClient(service)
    client.getInInterceptors.add(new ResponseCodeInterceptor(queue))

    queue
  }
}

class ResponseCodeInterceptor(queue: ConcurrentLinkedQueue[String]) extends AbstractPhaseInterceptor[Message](Phase.RECEIVE) {
  def handleMessage(message: Message): Unit = {
    queue.add(message.get(Message.RESPONSE_CODE).toString)
  }
}
