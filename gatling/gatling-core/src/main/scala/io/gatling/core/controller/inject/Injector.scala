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

import scala.concurrent.duration._

import io.gatling.commons.util.{ LongCounter, PushbackIterator }
import io.gatling.commons.util.Collections._
import io.gatling.commons.util.TimeHelper._
import io.gatling.core.controller.ControllerCommand
import io.gatling.core.scenario.Scenario
import io.gatling.core.session.Session
import io.gatling.core.stats.StatsEngine
import io.gatling.core.stats.writer.UserMessage

import akka.actor.{ Cancellable, Props, ActorSystem, ActorRef }

sealed trait InjectorCommand
object InjectorCommand {
  case object Start extends InjectorCommand
  case class OverrideStart(overrides: Map[String, UserStream]) extends InjectorCommand
  case object OverrideStop extends InjectorCommand
  case object Tick extends InjectorCommand
}

private[inject] case class UserStream(scenario: Scenario, stream: PushbackIterator[FiniteDuration])

private[inject] object Injection {
  val Empty = Injection(0, continue = false)
}

private[inject] case class Injection(count: Long, continue: Boolean) {
  def +(other: Injection): Injection =
    Injection(count + other.count, continue && other.continue)
}

object Injector {

  val InjectorActorName = "gatling-injector"

  def apply(system: ActorSystem, controller: ActorRef, statsEngine: StatsEngine, scenarios: List[Scenario]): ActorRef = {
    val userStreams = scenarios.map(scenario => scenario.name -> UserStream(scenario, new PushbackIterator(scenario.injectionProfile.allUsers))).toMap
    system.actorOf(Props(new Injector(controller, statsEngine, userStreams)), InjectorActorName)
  }
}

private[inject] class Injector(controller: ActorRef, statsEngine: StatsEngine, defaultStreams: Map[String, UserStream]) extends InjectorFSM {

  import InjectorState._
  import InjectorData._
  import InjectorCommand._

  private val tickPeriod = 1 second
  private val initialBatchWindow = tickPeriod * 2

  val userIdGen = new LongCounter

  private def inject(streams: Map[String, UserStream], batchWindow: FiniteDuration, startMillis: Long, count: Long, timer: Cancellable): State = {
    val injection = injectStreams(streams, batchWindow, startMillis)
    val newCount = injection.count + count
    if (injection.continue) {
      goto(Started) using StartedData(startMillis, newCount, timer)

    } else {
      controller ! ControllerCommand.InjectionStopped(newCount)
      timer.cancel()
      stop()
    }
  }

  private def injectStreams(streams: Map[String, UserStream], batchWindow: FiniteDuration, startTime: Long): Injection = {
    val injections = streams.values.map(withStream(_, batchWindow, startTime)(injectUser))
    val totalCount = injections.sumBy(_.count)
    val totalContinue = injections.exists(_.continue)
    Injection(totalCount, totalContinue)
  }

  private def startUser(scenario: Scenario, userId: Long): Unit = {
    val session = Session(scenario = scenario.name, userId = userId, onExit = scenario.onExit)
    scenario.entry ! session
    logger.info(s"Start user #${session.userId}")
    val userStart = UserMessage(session, io.gatling.core.stats.message.Start, session.startDate)
    statsEngine.logUser(userStart)
  }

  private def injectUser(scenario: Scenario, delay: FiniteDuration): Unit = {
    val userId = userIdGen.incrementAndGet()

    if (delay <= ZeroMs) {
      startUser(scenario, userId)
    } else {
      // Reduce the starting time to the millisecond precision to avoid flooding the scheduler
      system.scheduler.scheduleOnce(toMillisPrecision(delay))(startUser(scenario, userId))
    }
  }

  private def withStream(userStream: UserStream, batchWindow: FiniteDuration, startTime: Long)(f: (Scenario, FiniteDuration) => Unit): Injection = {

    val scenario = userStream.scenario
    val stream = userStream.stream

    if (stream.hasNext) {
      val batchTimeOffset = (nowMillis - startTime).millis
      val nextBatchTimeOffset = batchTimeOffset + batchWindow

      var continue = true
      var streamNonEmpty = true
      var count = 0L

      while (streamNonEmpty && continue) {

        val startingTime = stream.next()
        streamNonEmpty = stream.hasNext
        val delay = startingTime - batchTimeOffset
        continue = startingTime < nextBatchTimeOffset

        if (continue) {
          count += 1
          f(scenario, delay)
        } else {
          streamNonEmpty = true
          stream.pushback(startingTime)
        }
      }

      Injection(count, streamNonEmpty)
    } else {
      Injection.Empty
    }
  }

  startWith(WaitingToStart, NoData)

  when(WaitingToStart) {
    case Event(Start, NoData) =>
      val timer = system.scheduler.schedule(initialBatchWindow, tickPeriod, self, Tick)
      inject(defaultStreams, initialBatchWindow, nowMillis, 0, timer)
  }

  when(Started) {
    case Event(Tick, StartedData(startMillis, count, timer)) =>
      inject(defaultStreams, tickPeriod, startMillis, count, timer)

    case Event(OverrideStart(overrides), startedData: StartedData) =>
      goto(Overridden) using OverriddenData(startedData, overrides)

    case Event(OverrideStop, _) =>
      // out of band
      stay()
  }

  when(Overridden) {
    case Event(Tick, OverriddenData(StartedData(startMillis, count, timer), overrides)) =>
      // we still have to pull default streams, just drop them
      defaultStreams.values.foreach {
        withStream(_, tickPeriod, startMillis)((_, _) => ())
      }
      inject(overrides, tickPeriod, startMillis, count, timer)

    case Event(OverrideStart(overrides), OverriddenData(startedData, _)) =>
      goto(Overridden) using OverriddenData(startedData, overrides)

    case Event(OverrideStop, OverriddenData(startedData, _)) =>
      goto(Started) using startedData
  }
}
