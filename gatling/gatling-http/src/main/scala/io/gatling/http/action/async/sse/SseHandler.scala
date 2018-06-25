/**
 * Copyright 2011-2015 GatlingCorp (http://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gatling.http.action.async.sse

import java.net.InetAddress
import java.nio.charset.StandardCharsets.UTF_8
import java.util.concurrent.atomic.AtomicBoolean
import javax.xml.ws.http.HTTPException

import io.gatling.commons.util.TimeHelper.nowMillis
import io.gatling.http.action.async.{ AsyncTx, OnFailedOpen }

import akka.actor.ActorRef
import com.typesafe.scalalogging.StrictLogging
import org.asynchttpclient.AsyncHandler.State
import org.asynchttpclient.AsyncHandler.State.{ ABORT, CONTINUE }
import org.asynchttpclient._
import org.asynchttpclient.channel.NameResolution
import org.asynchttpclient.handler._
import org.asynchttpclient.netty.LazyNettyResponseBodyPart
import io.netty.handler.codec.http.HttpResponseStatus.OK

class SseHandler(tx: AsyncTx, sseActor: ActorRef) extends AsyncHandler[Unit]
    with AsyncHandlerExtensions
    with SseStream
    with EventStreamDispatcher
    with EventStreamParser
    with StrictLogging {

  private val done = new AtomicBoolean
  private var state: SseState = Opening

  override def onConnectionOpen(): Unit = ()

  override def onConnectionSuccess(connection: Any, address: InetAddress): Unit =
    state = Open

  override def onConnectionFailure(address: InetAddress): Unit = {}

  override def onConnectionPool(): Unit = ()

  override def onConnectionPooled(connection: Any): Unit = ()

  override def onConnectionOffer(connection: Any): Unit = ()

  override def onDnsResolved(address: Array[NameResolution]): Unit = ()

  override def onSslHandshakeCompleted(): Unit = ()

  override def onRetry(): Unit =
    if (done.get) logger.error("onRetry is not supposed to be called once done")

  override def onRequestSend(request: Any): Unit =
    logger.debug(s"Request $request has been sent by the http client")

  override def onStatusReceived(responseStatus: HttpResponseStatus): State = {

    val statusCode = responseStatus.getStatusCode
    logger.debug(s"Status $statusCode received for sse '${tx.requestName}")

    if (statusCode == OK.code) {
      sseActor ! OnOpen(tx, this, nowMillis)
      CONTINUE

    } else {
      onThrowable(new HTTPException(statusCode) {
        override def getMessage: String = s"Server returned http response with code $statusCode"
      })
      ABORT
    }
  }

  override def onHeadersReceived(headers: HttpResponseHeaders): State =
    if (done.get) ABORT
    else CONTINUE

  override def onBodyPartReceived(bodyPart: HttpResponseBodyPart): State = {
    if (done.get) {
      bodyPart.markUnderlyingConnectionAsToBeClosed()
      ABORT
    } else {
      val payload = bodyPart.asInstanceOf[LazyNettyResponseBodyPart].getBuf.toString(UTF_8)
      parse(payload)
      CONTINUE
    }
  }

  override def onCompleted(): Unit =
    if (done.compareAndSet(false, true))
      sseActor ! OnClose

  override def onThrowable(throwable: Throwable): Unit =
    if (done.compareAndSet(false, true))
      sendOnThrowable(throwable)

  def sendOnThrowable(throwable: Throwable): Unit = {
    val className = throwable.getClass.getName
    val errorMessage = throwable.getMessage match {
      case null => className
      case m    => s"$className: $m"
    }

    if (logger.underlying.isDebugEnabled)
      logger.debug(s"Request '${tx.requestName}' failed for user ${tx.session.userId}", throwable)
    else
      logger.info(s"Request '${tx.requestName}' failed for user ${tx.session.userId}: $errorMessage")

    state match {
      case Opening =>
        sseActor ! OnFailedOpen(tx, errorMessage, nowMillis)

      case Open =>
        sseActor ! OnThrowable(tx, errorMessage, nowMillis)

      case Closed =>
        logger.error(s"unexpected state closed with error message: $errorMessage")
    }
  }

  override def close(): Unit = onCompleted()

  override def dispatchEventStream(sse: ServerSentEvent): Unit = sseActor ! OnMessage(sse.asJSONString, nowMillis)
}

private sealed trait SseState
private case object Opening extends SseState
private case object Open extends SseState
private case object Closed extends SseState
