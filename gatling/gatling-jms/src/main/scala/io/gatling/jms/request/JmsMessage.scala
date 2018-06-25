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
package io.gatling.jms.request

import java.io.{ Serializable => JSerializable }

import io.gatling.core.session.Expression

/**
 * Provides the enumeration of JMSMessage types that the implementation supports
 * @author jasonk@bluedevel.com
 */
sealed trait JmsMessage
case class BytesJmsMessage(bytes: Expression[Array[Byte]]) extends JmsMessage
case class MapJmsMessage(map: Expression[Map[String, Any]]) extends JmsMessage
case class ObjectJmsMessage(o: Expression[JSerializable]) extends JmsMessage
case class TextJmsMessage(text: Expression[String]) extends JmsMessage
