package org.apache.cxf.transport.play

import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source, StreamConverters}
import javax.inject.Inject
import org.apache.cxf.message.Message
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future, Promise}

class CxfControllerStrict @Inject()(
    val transportFactory: PlayTransportFactory,
    controllerComponents: ControllerComponents
  )(
    implicit implicit val mat: Materializer,
    ec: ExecutionContext
  ) extends AbstractController(controllerComponents) with MessageDispatcher {

  def handle(path: String = ""): Action[RawBuffer] = Action.async(parse.raw(maxRequestSize)) {
    implicit request =>
      val delayedOutput = new DelayedOutputStream
      val replyPromise: Promise[Message] = Promise.apply()
      dispatchMessage(extractMessage, delayedOutput, replyPromise)

      replyPromise.future.map { outMessage =>
        val publisher = StreamConverters
          .asOutputStream()
          .mapMaterializedValue(
            os =>
              Future {
                delayedOutput.setTarget(os)
              }
          )
          .runWith(Sink.asPublisher(false))

        Ok.chunked(Source.fromPublisher(publisher))
          .as(outMessage.get(Message.CONTENT_TYPE).asInstanceOf[String])
      }
  }
}
