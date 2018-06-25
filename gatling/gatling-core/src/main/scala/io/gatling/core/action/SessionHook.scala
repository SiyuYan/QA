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

object SessionHook {
  def props(sessionFunction: Expression[Session], statsEngine: StatsEngine, next: ActorRef, interruptable: Boolean) =
    if (interruptable)
      Props(new SessionHook(sessionFunction, statsEngine, next) with Interruptable)
    else
      Props(new SessionHook(sessionFunction, statsEngine, next))
}

/**
 * Hook for interacting with the Session
 *
 * @constructor Constructs a SimpleAction
 * @param sessionFunction a function for manipulating the Session
 * @param statsEngine the StatsEngine
 * @param next the action to be executed after this one
 */
class SessionHook(sessionFunction: Expression[Session], val statsEngine: StatsEngine, val next: ActorRef) extends Chainable with Failable {

  /**
   * Applies the function to the Session
   *
   * @param session the session of the virtual user
   */
  def executeOrFail(session: Session) = sessionFunction(session).map(newSession => next ! newSession)
}
