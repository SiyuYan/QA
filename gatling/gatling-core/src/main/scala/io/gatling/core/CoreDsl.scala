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
package io.gatling.core

import scala.concurrent.duration._
import scala.reflect.ClassTag

import io.gatling.commons.validation._
import io.gatling.core.assertion.AssertionSupport
import io.gatling.core.body.BodyProcessors
import io.gatling.core.check.CheckSupport
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.controller.inject.InjectionSupport
import io.gatling.core.controller.throttle.ThrottlingSupport
import io.gatling.core.feeder.FeederSupport
import io.gatling.core.pause.PauseSupport
import io.gatling.core.session.{ Session, Expression }
import io.gatling.core.structure.{ ScenarioBuilder, StructureSupport }
import io.gatling.core.session._
import io.gatling.core.session.el._

trait CoreDsl extends StructureSupport
    with PauseSupport
    with CheckSupport
    with FeederSupport
    with InjectionSupport
    with ThrottlingSupport
    with AssertionSupport
    with CoreDefaultImplicits {

  def gzipBody(implicit configuration: GatlingConfiguration) = BodyProcessors.gzip
  def streamBody(implicit configuration: GatlingConfiguration) = BodyProcessors.stream

  implicit def stringToExpression[T: ClassTag](string: String): Expression[T] = string.el
  implicit def value2Success[T](value: T): Validation[T] = value.success
  implicit def value2Expression[T](value: T): Expression[T] = value.expressionSuccess

  def scenario(scenarioName: String): ScenarioBuilder = ScenarioBuilder(scenarioName)

  def WhiteList(patterns: String*) = io.gatling.core.filter.WhiteList(patterns.toList)

  def BlackList(patterns: String*) = io.gatling.core.filter.BlackList(patterns.toList)

  def flattenMapIntoAttributes(map: Expression[Map[String, Any]]): Expression[Session] =
    session => map(session).map(resolvedMap => session.setAll(resolvedMap))

  def ElFileBody = io.gatling.core.body.ElFileBody

  def StringBody(string: String) = io.gatling.core.body.CompositeByteArrayBody(string)

  def StringBody(string: Expression[String]) = io.gatling.core.body.StringBody(string)

  def RawFileBody = io.gatling.core.body.RawFileBody

  def ByteArrayBody = io.gatling.core.body.ByteArrayBody

  def InputStreamBody = io.gatling.core.body.InputStreamBody

  /***********************************/
  /** Duration implicit conversions **/
  /***********************************/

  implicit def integerToFiniteDuration(i: Integer): FiniteDuration = intToFiniteDuration(i.toInt)

  implicit def intToFiniteDuration(i: Int): FiniteDuration = i.seconds

  implicit def jlongToFiniteDuration(i: java.lang.Long): FiniteDuration = i.toLong.seconds
}
