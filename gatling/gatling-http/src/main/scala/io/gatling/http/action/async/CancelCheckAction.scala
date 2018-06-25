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

import io.gatling.commons.validation.Validation
import io.gatling.core.session._
import io.gatling.core.stats.StatsEngine
import io.gatling.http.action.RequestAction

import akka.actor.{ Actor, ActorRef, Props }

abstract class CancelCheckActionCreator[T <: CancelCheckAction: ClassTag] {
  def props(requestName: Expression[String], sseName: String, statsEngine: StatsEngine, next: ActorRef) =
    Props(implicitly[ClassTag[T]].runtimeClass, requestName, sseName, statsEngine, next)
}
abstract class CancelCheckAction(
    val requestName: Expression[String],
    actorName:       String,
    statsEngine:     StatsEngine,
    val next:        ActorRef
) extends RequestAction(statsEngine) with Actor with AsyncProtocolAction {

  def sendRequest(requestName: String, session: Session): Validation[Unit] =
    for (actor <- fetchActor(actorName, session)) yield actor ! CancelCheck(requestName, next, session)
}
