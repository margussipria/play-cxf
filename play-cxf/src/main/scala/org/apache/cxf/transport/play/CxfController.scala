package org.apache.cxf.transport.play

import java.io.{InputStream, OutputStream}
import javax.inject.Inject

import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source, StreamConverters}
import akka.util.ByteString
import org.apache.cxf.message.{Message, MessageImpl}
import play.api.mvc._

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future, Promise}

class CxfController @Inject() (
  transportFactory: PlayTransportFactory,
  controllerComponents: ControllerComponents
)(implicit implicit val mat: Materializer, ec: ExecutionContext) extends AbstractController(controllerComponents) {

  val maxRequestSize: Int = 1024 * 1024

  def handle(path: String = ""): Action[RawBuffer] = Action.async(parse.raw(maxRequestSize)) { implicit request =>
    val delayedOutput = new DelayedOutputStream
    val replyPromise: Promise[Message] = Promise.apply()
    dispatchMessage(extractMessage, delayedOutput, replyPromise)

    replyPromise.future.map { outMessage =>
      val publisher = StreamConverters.asOutputStream().mapMaterializedValue(os => Future {
        delayedOutput.setTarget(os)
      }).runWith(Sink.asPublisher(false))

      Ok
        .chunked(Source.fromPublisher(publisher))
        .as(outMessage.get(Message.CONTENT_TYPE).asInstanceOf[String])
    }
  }

  private def extractMessage()(implicit request: Request[RawBuffer]) = {
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

  private def endpointAddress()(implicit request: Request[RawBuffer]) = "play://" + request.host + request.path

  private def headersAsJava()(implicit request: Request[RawBuffer]) = {
    request.headers.toMap.mapValues(_.asJava).asJava
  }

  private def dispatchMessage(
    inMessage: Message,
    output: OutputStream,
    replyPromise: Promise[Message]
  )(implicit request: Request[RawBuffer]) {

    val dOpt = Option(transportFactory.getDestination(endpointAddress)).orElse(
      Option(transportFactory.getDestination(request.path))
    )
    dOpt match {
      case Some(destination) =>
        inMessage.put(Message.ENDPOINT_ADDRESS, destination.getFactoryKey)
        destination.dispatchMessage(inMessage, output, replyPromise)
      case _ =>
        replyPromise.failure(
          new IllegalArgumentException(s"Destination not found: [$endpointAddress] ${transportFactory.getDestinationsDebugInfo}")
        )
    }
  }
}
