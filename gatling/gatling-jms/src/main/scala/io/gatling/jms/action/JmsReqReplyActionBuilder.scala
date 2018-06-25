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
package io.gatling.jms.action

import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.protocol.ProtocolComponentsRegistry
import io.gatling.core.structure.ScenarioContext
import io.gatling.jms.protocol.{ JmsProtocol, JmsComponents }
import io.gatling.jms.request.JmsAttributes

import akka.actor.ActorRef

case class JmsReqReplyActionBuilder(attributes: JmsAttributes)(implicit configuration: GatlingConfiguration) extends ActionBuilder {

  def jmsComponents(protocolComponentsRegistry: ProtocolComponentsRegistry): JmsComponents =
    protocolComponentsRegistry.components(JmsProtocol.JmsProtocolKey)

  def build(ctx: ScenarioContext, next: ActorRef) = {
    import ctx._
    val statsEngine = coreComponents.statsEngine
    val tracker = system.actorOf(JmsRequestTrackerActor.props(statsEngine), actorName("jmsRequestTracker"))
    system.actorOf(JmsReqReplyAction.props(attributes, jmsComponents(protocolComponentsRegistry).jmsProtocol, tracker, statsEngine, next), actorName("jmsReqReply"))
  }
}
