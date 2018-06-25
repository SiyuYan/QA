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
package io.gatling.core.session

import io.gatling.commons.stats.{ OK, Status }
import io.gatling.commons.util.TimeHelper.nowMillis
import io.gatling.commons.validation._

import akka.actor.ActorRef
import com.typesafe.scalalogging.LazyLogging

sealed trait Block

sealed trait CounterBlock extends Block {
  def counterName: String
}

object LoopBlock extends LazyLogging {

  def unapply(block: Block): Option[String] = block match {
    case ExitASAPLoopBlock(counterName, _, _) => Some(counterName)
    case ExitOnCompleteLoopBlock(counterName) => Some(counterName)
    case _                                    => None
  }

  def continue(continueCondition: Expression[Boolean], session: Session): Boolean = continueCondition(session) match {
    case Success(eval) => eval
    case Failure(message) =>
      logger.error(s"Condition evaluation crashed with message '$message', exiting loop")
      false
  }
}

case class ExitOnCompleteLoopBlock(counterName: String) extends CounterBlock

case class ExitASAPLoopBlock(counterName: String, condition: Expression[Boolean], loopActor: ActorRef) extends CounterBlock

case class TryMaxBlock(counterName: String, tryMaxActor: ActorRef, status: Status = OK) extends CounterBlock

case class GroupBlock(hierarchy: List[String], startDate: Long = nowMillis, cumulatedResponseTime: Int = 0, status: Status = OK) extends Block
