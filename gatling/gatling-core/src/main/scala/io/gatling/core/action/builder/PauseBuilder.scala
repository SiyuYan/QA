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
package io.gatling.core.action.builder

import scala.concurrent.duration.Duration

import io.gatling.core.action.Pause
import io.gatling.core.pause.{ PauseType, Disabled }
import io.gatling.core.session.Expression
import io.gatling.core.structure.ScenarioContext

import akka.actor.ActorRef

/**
 * Builder for the 'pause' action.
 *
 * @constructor create a new PauseBuilder
 * @param duration mean duration of the generated pause
 * @param force if the global pause type has to be overridden
 */
class PauseBuilder(duration: Expression[Duration], force: Option[PauseType]) extends ActionBuilder {

  def build(ctx: ScenarioContext, next: ActorRef) = {

    import ctx._

    force.getOrElse(ctx.pauseType) match {
      case Disabled => next
      case pauseType =>
        val generator = pauseType.generator(duration)
        system.actorOf(Pause.props(generator, coreComponents.statsEngine, next), actorName("pause"))
    }
  }
}
