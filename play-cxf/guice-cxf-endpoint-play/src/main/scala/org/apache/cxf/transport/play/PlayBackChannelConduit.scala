package org.apache.cxf.transport.play

import java.io.{IOException, InputStream, OutputStream}
import java.util.logging.Logger

import org.apache.cxf.common.logging.LogUtils
import org.apache.cxf.message.Message
import org.apache.cxf.transport.AbstractConduit
import org.apache.cxf.ws.addressing.EndpointReferenceUtils

import scala.concurrent.Promise

class PlayBackChannelConduit(destination: PlayDestination, promise: Promise[Message])
  extends AbstractConduit(EndpointReferenceUtils.getAnonymousEndpointReference) {

  private val LOG = LogUtils.getL7dLogger(classOf[PlayBackChannelConduit])

  def prepare(message: Message): Unit = {
    message.setContent(classOf[OutputStream], new DelayedOutputStream)
  }

  override def close(message: Message): Unit = {
    super.close(message)

    Option(message.getExchange).foreach { exchange =>
      Option(exchange.getInMessage) foreach { inMessage =>
        Option(inMessage.getContent(classOf[InputStream])) foreach { is =>
          try {
            is.close()
            inMessage.removeContent(classOf[InputStream])
          } catch {
            case _: IOException =>
          }
        }
      }
    }

    promise.success(message)
  }

  override def getLogger: Logger = LOG
}
