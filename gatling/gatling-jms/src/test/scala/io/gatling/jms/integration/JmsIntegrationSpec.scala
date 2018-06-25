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
package io.gatling.jms.integration

import javax.jms.TextMessage

import io.gatling.core.CoreDsl
import io.gatling.core.config.GatlingConfiguration
import io.gatling.jms._
import io.gatling.jms.request.JmsQueue

class JmsIntegrationSpec extends JmsMockingSpec with CoreDsl with JmsDsl {

  implicit val configuration = GatlingConfiguration.loadForTest()

  "gatling-jms" should "send and receive JMS message" in {

    val requestQueue = JmsQueue("request")

    jmsMock(requestQueue, {
      case tm: TextMessage => tm.getText.toUpperCase
    })

    val session = runScenario(
      scenario("Jms upperCase")
        .exec(
          jms("toUpperCase")
            .reqreply
            .destination(requestQueue)
            .textMessage("<hello>hi</hello>")
            .check(xpath("/HELLO").find.saveAs("content"))
        )
    )

    session.isFailed shouldBe false
    session("content").as[String] shouldBe "HI"
  }
}
