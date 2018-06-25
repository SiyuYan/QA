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

import io.gatling.commons.util.TimeHelper.nowMillis
import io.gatling.commons.validation.{ Failure, Success }
import io.gatling.core.action.Interruptable
import io.gatling.core.session.{ Expression, Session }
import io.gatling.core.stats.StatsEngine
import io.gatling.http.action.async.AsyncTx
import io.gatling.http.check.async.AsyncCheckBuilder
import io.gatling.http.protocol.HttpComponents

import akka.actor.{ Props, ActorRef }
import org.asynchttpclient.Request

object SseOpenAction {

  def props(
    requestName:    Expression[String],
    sseName:        String,
    request:        Expression[Request],
    checkBuilder:   Option[AsyncCheckBuilder],
    statsEngine:    StatsEngine,
    httpComponents: HttpComponents,
    next:           ActorRef
  ) =
    Props(new SseOpenAction(requestName, sseName, request, checkBuilder, statsEngine, httpComponents, next))
}

class SseOpenAction(
    requestName:     Expression[String],
    sseName:         String,
    request:         Expression[Request],
    checkBuilder:    Option[AsyncCheckBuilder],
    val statsEngine: StatsEngine,
    httpComponents:  HttpComponents,
    val next:        ActorRef
) extends Interruptable with SseAction {

  import httpComponents._

  override def execute(session: Session): Unit = {

      def open(tx: AsyncTx): Unit = {
        logger.info(s"Opening and getting sse '$sseName': Scenario '${session.scenario}', UserId #${session.userId}")
        val sseActor = context.actorOf(SseActor.props(sseName, statsEngine), actorName("sseActor"))
        SseTx.start(tx, sseActor, httpEngine)
      }

    fetchActor(sseName, session) match {
      case _: Success[_] =>
        Failure(s"Unable to create a new SSE with name $sseName: Already exists")
      case _ =>
        for {
          requestName <- requestName(session)
          request <- request(session)
          check = checkBuilder.map(_.build)
        } yield open(AsyncTx(session, next, requestName, request, httpProtocol, nowMillis, check = check))
    }
  }
}
