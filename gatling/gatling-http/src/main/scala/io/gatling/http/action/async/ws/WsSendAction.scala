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
package io.gatling.http.action.async.ws

import io.gatling.core.stats.StatsEngine

import akka.actor.{ Props, ActorRef }
import io.gatling.core.session.{ Expression, Session }
import io.gatling.http.action.RequestAction
import io.gatling.http.check.async.AsyncCheckBuilder

object WsSendAction {
  def props(
    requestName:  Expression[String],
    wsName:       String,
    message:      Expression[WsMessage],
    checkBuilder: Option[AsyncCheckBuilder],
    statsEngine:  StatsEngine,
    next:         ActorRef
  ) =
    Props(new WsSendAction(requestName, wsName, message, checkBuilder, statsEngine, next))
}

class WsSendAction(
  val requestName: Expression[String],
  wsName:          String,
  message:         Expression[WsMessage],
  checkBuilder:    Option[AsyncCheckBuilder],
  statsEngine:     StatsEngine,
  val next:        ActorRef
)
    extends RequestAction(statsEngine)
    with WsAction {

  override def sendRequest(requestName: String, session: Session) =
    for {
      wsActor <- fetchActor(wsName, session)
      resolvedMessage <- message(session)
      check = checkBuilder.map(_.build)
    } yield wsActor ! Send(requestName, resolvedMessage, check, next, session)
}
