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
package io.gatling.core.controller.throttle

import scala.concurrent.duration._

import akka.actor.ActorRef

sealed trait ThrottlerControllerCommand

object ThrottlerControllerCommand {
  case object Start extends ThrottlerControllerCommand
  case class OverrideStart(overrides: Throttlings) extends ThrottlerControllerCommand
  case object OverrideStop extends ThrottlerControllerCommand
  case object Tick extends ThrottlerControllerCommand
}

private[throttle] class ThrottlerController(throttler: ActorRef, defaults: Throttlings) extends ThrottlerControllerFSM {

  import ThrottlerControllerState._
  import ThrottlerControllerData._
  import ThrottlerControllerCommand._

  def notifyThrottler(throttlings: Throttlings, tick: Int): Unit = {

    val throttles = Throttles(
      global = throttlings.global.map(p => new Throttle(p.limit(tick))),
      perScenario = throttlings.perScenario.mapValues(p => new Throttle(p.limit(tick)))
    )

    throttler ! throttles
  }

  startWith(WaitingToStart, NoData)

  when(WaitingToStart) {

    case Event(Start, NoData) =>
      system.scheduler.schedule(Duration.Zero, 1 second, self, Tick)
      notifyThrottler(defaults, 0)
      goto(Started) using StartedData(0)
  }

  when(Started) {

    case Event(Tick, StartedData(tick)) =>
      notifyThrottler(defaults, tick)
      stay() using StartedData(tick + 1)

    case Event(OverrideStart(overrides), StartedData(tick)) =>
      goto(Overridden) using OverrideData(overrides, tick)

    case Event(OverrideStop, _) =>
      // out fo band
      stay()
  }

  when(Overridden) {

    case Event(Tick, OverrideData(overrides, tick)) =>
      notifyThrottler(overrides, tick)
      stay() using OverrideData(overrides, tick + 1)

    case Event(OverrideStart(newOverrides), OverrideData(_, tick)) =>
      stay() using OverrideData(newOverrides, tick)

    case Event(OverrideStop, OverrideData(_, tick)) =>
      goto(Started) using StartedData(tick)
  }
}
