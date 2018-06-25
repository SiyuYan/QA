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

import io.gatling.commons.util.RoundRobin
import io.gatling.commons.validation.SuccessWrapper
import io.gatling.core.action.Switch
import io.gatling.core.session.Expression
import io.gatling.core.structure.{ ScenarioContext, ChainBuilder }

import akka.actor.ActorRef

class RoundRobinSwitchBuilder(possibilities: List[ChainBuilder]) extends ActionBuilder {

  require(possibilities.size >= 2, "Round robin switch requires at least 2 possibilities")

  def build(ctx: ScenarioContext, next: ActorRef) = {

    val possibleActions = possibilities.map(_.build(ctx, next)).toArray
    val roundRobin = RoundRobin(possibleActions)

    val nextAction: Expression[ActorRef] = _ => roundRobin.next.success

    ctx.system.actorOf(Switch.props(nextAction, ctx.coreComponents.statsEngine, next), actorName("roundRobinSwitch"))
  }
}
