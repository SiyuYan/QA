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
package io.gatling.jms

import io.gatling.core.action.builder.ActionBuilder
import io.gatling.jms.check.JmsCheckSupport
import io.gatling.jms.protocol.{ JmsProtocolBuilderBase, JmsProtocolBuilder, JmsProtocol }
import io.gatling.jms.request.{ JmsTopic, JmsQueue, JmsRequestBuilderBase, JmsRequestBuilder }

trait JmsDsl extends JmsCheckSupport {

  val jms = JmsProtocolBuilderBase

  /**
   * DSL text to start the jms builder
   *
   * @param requestName human readable name of request
   * @return a PingBuilder instance which can be used to build up a ping
   */
  def jms(requestName: String) = JmsRequestBuilderBase(requestName)

  /**
   * Convert a JmsProtocolBuilder to a JmsProtocol
   * <p>
   * Simplifies the API somewhat (you can pass the builder reference to the scenario .protocolConfig() method)
   */
  implicit def jmsProtocolBuilder2jmsProtocol(builder: JmsProtocolBuilder): JmsProtocol = builder.build

  implicit def jmsRequestBuilder2ActionBuilder(builder: JmsRequestBuilder): ActionBuilder = builder.build()

  def topic(name: String) = JmsTopic(name)
  def queue(name: String) = JmsQueue(name)
}
