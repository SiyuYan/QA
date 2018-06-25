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

import scala.concurrent.duration._

import io.gatling.BaseSpec
import io.gatling.commons.util.TimeHelper._

class TimeHelperSpec extends BaseSpec {

  "TimeHelper" should "toMillisPrecision should work correctly" in {
    val t1 = 100.nanoseconds
    toMillisPrecision(t1).toNanos shouldBe 0

    val t2 = 1234.microseconds
    toMillisPrecision(t2).toMicros shouldBe 1000

    val t3 = 1.minute
    toMillisPrecision(t3).toMicros shouldBe (1 * 60 * 1000 * 1000)
  }
}
