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
package io.gatling.core.util.cache

import io.gatling.BaseSpec

class ThreadSafeCacheSpec extends BaseSpec {

  "ThreadSafeCache" should "throw an IllegalArgumentException if the capacity is negative" in {
    an[IllegalArgumentException] should be thrownBy ThreadSafeCache[String, String](-1)
  }

  it should "be disabled if the capacity is 0" in {
    val cache = ThreadSafeCache[String, String](0)
    cache.enabled shouldBe false
  }

  it should "start with an initially empty cache" in {
    val cache = ThreadSafeCache[String, String](1)
    cache.cache shouldBe empty
  }

  it should "store values in the cache" in {
    val cache = ThreadSafeCache[String, String](1)
    cache.getOrElsePutIfAbsent("key", "value")
    cache.cache should contain key "key"
  }
}
