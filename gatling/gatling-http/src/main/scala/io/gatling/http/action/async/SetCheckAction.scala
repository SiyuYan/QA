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
package io.gatling.http.action.async

import scala.reflect.ClassTag

import io.gatling.core.session._
import io.gatling.core.stats.StatsEngine
import io.gatling.http.action.RequestAction
import io.gatling.http.check.async._

import akka.actor.{ ActorRef, Props }

abstract class SetCheckActionCreator[T <: SetCheckAction: ClassTag] {
  def props(requestName: Expression[String], checkBuilder: AsyncCheckBuilder, wsName: String, statsEngine: StatsEngine, next: ActorRef) =
    Props(implicitly[ClassTag[T]].runtimeClass, requestName, checkBuilder, wsName, statsEngine, next)
}
abstract class SetCheckAction(
    val requestName: Expression[String],
    checkBuilder:    AsyncCheckBuilder,
    wsName:          String,
    statsEngine:     StatsEngine,
    val next:        ActorRef
) extends RequestAction(statsEngine) with AsyncProtocolAction {

  override def sendRequest(requestName: String, session: Session) =
    for (wsActor <- fetchActor(wsName, session)) yield wsActor ! SetCheck(requestName, checkBuilder.build, next, session)
}
