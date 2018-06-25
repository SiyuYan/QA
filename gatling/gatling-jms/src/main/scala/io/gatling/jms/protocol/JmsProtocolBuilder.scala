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
package io.gatling.jms.protocol

import javax.jms.DeliveryMode

import io.gatling.core.config.Credentials

/**
 * JmsProtocolBuilder allows building of the JMS protocol
 * <p>
 * This allows multiple scenarios or jms methods to refer to a single protocol configuration.
 * <p>
 * See your JMS provider documentation for information on the values to set here.
 */
case object JmsProtocolBuilderBase {

  def connectionFactoryName(cfn: String) = JmsProtocolBuilderUrlStep(cfn)
}

case class JmsProtocolBuilderUrlStep(connectionFactoryName: String) {

  def url(theUrl: String) =
    JmsProtocolBuilderContextFactoryStep(connectionFactoryName, theUrl)
}

case class JmsProtocolBuilderContextFactoryStep(
    connectionFactoryName: String,
    url:                   String,
    credentials:           Option[Credentials] = None,
    anonymousConnect:      Boolean             = true
) {

  def credentials(user: String, password: String) = copy(credentials = Some(Credentials(user, password)))

  def disableAnonymousConnect = copy(anonymousConnect = false)

  def contextFactory(cf: String) =
    JmsProtocolBuilderListenerCountStep(connectionFactoryName, url, credentials, anonymousConnect, cf)
}

case class JmsProtocolBuilderListenerCountStep(
    connectionFactoryName: String,
    url:                   String,
    credentials:           Option[Credentials],
    anonymousConnect:      Boolean,
    contextFactory:        String
) {

  def listenerCount(count: Int) = {
    require(count > 0, "JMS response listener count must be at least 1")
    JmsProtocolBuilder(connectionFactoryName, url, credentials, anonymousConnect, contextFactory, count)
  }
}

case class JmsProtocolBuilder(
    connectionFactoryName: String,
    url:                   String,
    credentials:           Option[Credentials],
    anonymousConnect:      Boolean,
    contextFactory:        String,
    listenerCount:         Int,
    deliveryMode:          Int                 = DeliveryMode.NON_PERSISTENT,
    messageMatcher:        JmsMessageMatcher   = MessageIDMessageMatcher,
    receiveTimeout:        Option[Long]        = None
) {

  def usePersistentDeliveryMode = copy(deliveryMode = DeliveryMode.PERSISTENT)
  def useNonPersistentDeliveryMode = copy(deliveryMode = DeliveryMode.NON_PERSISTENT)
  def matchByMessageID = messageMatcher(MessageIDMessageMatcher)
  def matchByCorrelationID = messageMatcher(CorrelationIDMessageMatcher)
  def messageMatcher(matcher: JmsMessageMatcher) = copy(messageMatcher = matcher)
  def receiveTimeout(timeout: Long): JmsProtocolBuilder = copy(receiveTimeout = Some(timeout))

  def build = new JmsProtocol(
    contextFactory = contextFactory,
    connectionFactoryName = connectionFactoryName,
    url = url,
    credentials = credentials,
    anonymousConnect = anonymousConnect,
    listenerCount = listenerCount,
    deliveryMode = deliveryMode,
    messageMatcher = messageMatcher,
    receiveTimeout = receiveTimeout
  )
}
