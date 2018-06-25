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
package io.gatling.core.controller.inject

import scala.concurrent.duration.FiniteDuration

import io.gatling.commons.util.Collections._

/**
 * This class represents the configuration of a scenario
 *
 * @param injectionSteps the number of users that will behave as this scenario says
 */
case class InjectionProfile(injectionSteps: Iterable[InjectionStep]) {
  val userCount = injectionSteps.sumBy(_.users)
  val allUsers = injectionSteps.foldRight(Iterator.empty: Iterator[FiniteDuration]) { (step, iterator) => step.chain(iterator) }
}
