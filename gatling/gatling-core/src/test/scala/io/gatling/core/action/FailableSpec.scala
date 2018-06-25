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
package io.gatling.core.action

import io.gatling.AkkaSpec
import io.gatling.commons.validation._
import io.gatling.core.session.Session

import akka.actor.ActorRef
import akka.testkit._

class FailableSpec extends AkkaSpec {

  class TestAction(val next: ActorRef, fail: Boolean) extends Action with Chainable with Failable {
    var hasRun = false

    override def executeOrFail(session: Session) =
      if (fail) "woops".failure
      else {
        hasRun = true
        1.success
      }
  }

  "A Failable Action" should "call the execute method when receiving a Session" in {
    val testAction = TestActorRef(new TestAction(self, fail = false))

    testAction.underlyingActor.hasRun shouldBe false
    testAction ! Session("scenario", 0)

    testAction.underlyingActor.hasRun shouldBe true
  }

  it should "send the session, failed, to the next actor when executeOrFail returns a Failure" in {
    val testAction = TestActorRef(new TestAction(self, fail = true))
    val session = Session("scenario", 0)

    testAction ! session

    expectMsg(session.markAsFailed)
  }
}
