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
package io.gatling.commons.util

import java.lang.System.{ currentTimeMillis, nanoTime }

import scala.concurrent.duration._

object TimeHelper {

  val ZeroMs = 0 millisecond

  val CurrentTimeMillisReference = currentTimeMillis
  val NanoTimeReference = nanoTime

  def computeTimeMillisFromNanos(nanos: Long) = (nanos - NanoTimeReference) / 1000000 + CurrentTimeMillisReference
  def nowMillis = computeTimeMillisFromNanos(nanoTime)
  def nowSeconds = computeTimeMillisFromNanos(nanoTime) / 1000

  def toMillisPrecision(t: FiniteDuration): FiniteDuration =
    t.unit match {
      case MICROSECONDS | NANOSECONDS => t.toMillis.milliseconds
      case _                          => t
    }
}
