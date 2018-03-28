package org.apache.cxf.transport.play

import java.io.InputStream

import javax.inject.{Inject, Singleton}

import akka.stream.scaladsl.StreamConverters
import akka.util.ByteString
import org.apache.cxf.message.{Message, MessageImpl}
import play.api.mvc._

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CxfController @Inject() (
  transportFactory: PlayTransportFactory,
  controllerComponents: ControllerComponents
)(implicit ec: ExecutionContext) extends AbstractController(controllerComponents) {

  val maxRequestSize: Int = 1024 * 1024

  def handle(path: String = ""): Action[RawBuffer] = Action.async(parse.raw(maxRequestSize)) { implicit request =>
    Future {
      val destination: PlayDestination = getDestination

      val message = extractMessage

      val source = StreamConverters.asOutputStream().mapMaterializedValue(output => Future {
        message.put(Message.ENDPOINT_ADDRESS, destination.getFactoryKey)
        destination.dispatchMessage(message, output)
      })

      Ok
        .chunked(source)
        .as(message.get(Message.CONTENT_TYPE).asInstanceOf[String])
    }
  }

  private def extractMessage()(implicit request: Request[RawBuffer]): Message = {
    val msg: Message = new MessageImpl
    msg.put(Message.HTTP_REQUEST_METHOD, request.method)
    msg.put(Message.REQUEST_URL, request.path)
    msg.put(Message.QUERY_STRING, request.rawQueryString)
    msg.put(Message.PROTOCOL_HEADERS, headersAsJava)
    msg.put(Message.CONTENT_TYPE, request.headers.get(Message.CONTENT_TYPE).orNull)
    msg.put(Message.ACCEPT_CONTENT_TYPE, request.headers.get(Message.ACCEPT_CONTENT_TYPE).orNull)
    msg.put("Remote-Address", request.remoteAddress)

    request.body.asBytes() foreach { arr: ByteString =>
      msg.setContent(classOf[InputStream], arr.iterator.asInputStream)
    }

    msg
  }

  private def endpointAddress(implicit request: Request[RawBuffer]): String = "play://" + request.host + request.path

  private def headersAsJava(implicit request: Request[RawBuffer]): java.util.Map[String, java.util.List[String]] = {
    request.headers.toMap.mapValues(_.asJava).asJava
  }

  def getDestination(implicit request: Request[RawBuffer]): PlayDestination = {
    Option(transportFactory.getDestination(endpointAddress)).orElse(
      Option(transportFactory.getDestination(request.path))
    ) getOrElse {
      throw new IllegalArgumentException(s"Destination not found: [$endpointAddress] ${transportFactory.getDestinationsDebugInfo}")
    }
  }
}
