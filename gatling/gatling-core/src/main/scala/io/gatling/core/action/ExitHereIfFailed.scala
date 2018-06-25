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
package io.gatling.core.action

import io.gatling.commons.stats.KO
import io.gatling.commons.util.TimeHelper.nowMillis
import io.gatling.core.session.{ GroupBlock, Session }
import io.gatling.core.stats.StatsEngine

import akka.actor.{ Props, ActorRef }

object ExitHereIfFailed {
  def props(exit: ActorRef, statsEngine: StatsEngine, next: ActorRef) =
    Props(new ExitHereIfFailed(exit, statsEngine, next))
}

class ExitHereIfFailed(exit: ActorRef, statsEngine: StatsEngine, val next: ActorRef) extends Chainable {

  def execute(session: Session): Unit = {

    val nextStep = session.status match {
      case KO =>
        val now = nowMillis

        session.blockStack.foreach {
          case group: GroupBlock => statsEngine.logGroupEnd(session, group, now)
          case _                 =>
        }

        exit

      case _ => next
    }

    nextStep ! session
  }
}
