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
package io.gatling.core.stats.writer

import scala.reflect.ClassTag
import scala.util.control.NonFatal

import io.gatling.commons.util.TypeHelper.typeMatches

import akka.actor.FSM.NullFunction

/**
 * Abstract class for all DataWriters
 *
 * These writers are responsible for writing the logs that will be read to
 * generate the statistics
 */
abstract class DataWriter[T <: DataWriterData: ClassTag] extends DataWriterFSM {

  startWith(Uninitialized, NoData)

  def onInit(init: Init): T

  def onFlush(data: T): Unit

  def onCrash(cause: String, data: T): Unit

  def onStop(data: T): Unit

  def onMessage(message: LoadEventMessage, data: T): Unit

  when(Uninitialized) {
    case Event(init: Init, NoData) =>
      logger.info("Initializing")
      try {
        val newState = onInit(init)
        logger.info("Initialized")
        sender ! true
        goto(Initialized) using newState
      } catch {
        case NonFatal(e) =>
          logger.error("DataWriter failed to initialize", e)
          sender ! false
          goto(Terminated)
      }
  }

  when(Initialized) {
    case Event(Flush, data: Any) if typeMatches[T](data) =>
      onFlush(data.asInstanceOf[T])
      stay()

    case Event(Stop, data: Any) if typeMatches[T](data) =>
      onStop(data.asInstanceOf[T])
      sender ! true
      goto(Terminated) using NoData

    case Event(Crash(cause), data: Any) if typeMatches[T](data) =>
      onCrash(cause, data.asInstanceOf[T])
      goto(Terminated) using NoData

    case Event(message: LoadEventMessage, data: Any) if typeMatches[T](data) =>
      onMessage(message, data.asInstanceOf[T])
      stay()
  }

  when(Terminated)(NullFunction)

  whenUnhandled {
    case Event(m, data) =>
      logger.info(s"Can't handle $m in state $stateName")
      stay()
  }
}
