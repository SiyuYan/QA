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

import io.gatling.core.stats.StatsEngine

import akka.actor.{ Props, ActorRef }
import io.gatling.core.session.{ Expression, Session }

object If {
  def props(condition: Expression[Boolean], thenNext: ActorRef, elseNext: ActorRef, statsEngine: StatsEngine, next: ActorRef) =
    Props(new If(condition, thenNext, elseNext, statsEngine, next))
}

/**
 * A conditional Action
 *
 * @constructor create an IfAction
 * @param condition the condition that decides whether to execute thenNext or elseNext
 * @param thenNext the chain of actions executed if condition evaluates to true
 * @param elseNext chain of actions executed if condition evaluates to false
 * @param statsEngine the StatsEngine
 * @param next chain of actions executed if condition evaluates to false and elseNext equals None
 */
class If(condition: Expression[Boolean], thenNext: ActorRef, elseNext: ActorRef, val statsEngine: StatsEngine, val next: ActorRef) extends Interruptable with Failable {

  /**
   * Evaluates the condition and decides what to do next
   *
   * @param session the session of the virtual user
   */
  def executeOrFail(session: Session) = condition(session).map { condition =>
    val n = if (condition) thenNext else elseNext
    n ! session
  }
}
