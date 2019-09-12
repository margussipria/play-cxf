package org.apache.cxf.transport.play

import java.io.OutputStream

import javax.inject.{Inject, Singleton}

import akka.stream.scaladsl.StreamConverters
import org.apache.cxf.message.Message
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Failure

@Singleton
class CxfController @Inject() (
  transportFactory: PlayTransportFactory,
  controllerComponents: ControllerComponents,
  messageExtractor: MessageExtractor
)(implicit ec: ExecutionContext) extends AbstractController(controllerComponents) {

  val maxRequestSize: Int = 1024 * 1024

  def handle(path: String = ""): Action[RawBuffer] = Action.async(parse.raw(maxRequestSize)) { implicit request =>
    val messagePromise = Promise[Message]()

    Future {
      val message = messageExtractor.extractMessage
      message.put(PlayDestination.PLAY_MESSAGE_PROMISE, messagePromise)

      getDestination.dispatchMessage(message)
    } andThen {
      case Failure(exception) =>
        messagePromise.tryFailure(exception)
    }

    messagePromise.future.map { message =>

      val source = StreamConverters.asOutputStream().mapMaterializedValue(outputStream => Future {
        val delayedOutputStream = message.getContent(classOf[OutputStream]).asInstanceOf[DelayedOutputStream]

        delayedOutputStream.flush()
        delayedOutputStream.setTarget(outputStream)
      })

      val responseCode = Option(message.get(Message.RESPONSE_CODE)) map (_.toString) map (_.toInt) getOrElse OK
      val contentType = message.get(Message.CONTENT_TYPE).asInstanceOf[String]

      Status(responseCode).chunked(source).as(contentType)
    }
  }

  private def endpointAddress(implicit request: Request[RawBuffer]): String = "play://" + request.host + request.path

  def getDestination(implicit request: Request[RawBuffer]): PlayDestination = {
    Option(transportFactory.getDestination(endpointAddress)).orElse(
      Option(transportFactory.getDestination(request.path))
    ) getOrElse {
      throw new IllegalArgumentException(s"Destination not found: [$endpointAddress] ${transportFactory.getDestinationsDebugInfo}")
    }
  }
}
