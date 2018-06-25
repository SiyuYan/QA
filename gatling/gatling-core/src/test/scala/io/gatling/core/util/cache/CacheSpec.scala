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

class CacheSpec extends BaseSpec {

  "Cache.get" should "return the value wrapped in an Option if key present in cache" in {
    val cache = Cache[String, String](1)
    val cacheWithValue = cache + ("key" -> "value")

    cacheWithValue.get("key") shouldBe Some("value")
  }

  it should "return None if the key is not present in cache" in {
    val cache = Cache[String, String](1)

    cache.get("key") shouldBe None
  }

  "Cache.+" should "return the same instance when adding a key already in cache" in {
    val cache = Cache[String, String](1)
    val cacheWithValue = cache + ("key" -> "value")
    val cacheWithSameValue = cacheWithValue + ("key" -> "value")

    cacheWithSameValue should be theSameInstanceAs cacheWithValue
  }

  it should "overwrite the key first put in the cache when max capacity has been reached" in {
    val cache = Cache[String, String](2)
    val cacheWithFirstValue = cache + ("key" -> "value")
    val cacheWithSecondValue = cacheWithFirstValue + ("key2" -> "value2")
    val cacheWithThirdValue = cacheWithSecondValue + ("key3" -> "value3")

    cacheWithThirdValue.get("key") shouldBe None
    cacheWithThirdValue.get("key2") shouldBe Some("value2")
    cacheWithThirdValue.get("key3") shouldBe Some("value3")
  }

  "Cache.-" should "remove a key from the cache " in {
    val cache = Cache[String, String](1)
    val cacheWithValue = cache + ("key" -> "value")
    val cacheWithValueRemoved = cacheWithValue - "key"

    cacheWithValueRemoved.get("key") shouldBe None
  }

  it should "return the same instance when removing a key absent from cache" in {
    val cache = Cache[String, String](1)

    val cacheWithValue = cache + ("key" -> "value")
    val cacheWithValueRemoved = cacheWithValue - "key"
    val cacheWithSameValueRemoved = cacheWithValueRemoved - "key"

    cacheWithValueRemoved should be theSameInstanceAs cacheWithSameValueRemoved
  }
}
