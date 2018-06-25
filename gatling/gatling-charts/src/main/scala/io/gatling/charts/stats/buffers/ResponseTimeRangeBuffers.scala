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
package io.gatling.charts.stats.buffers

import scala.collection.mutable

import io.gatling.charts.stats.{ GroupRecord, RequestRecord }
import io.gatling.commons.stats.{ Group, KO, Status }
import io.gatling.core.config.GatlingConfiguration

private[stats] trait ResponseTimeRangeBuffers {

  val responseTimeRangeBuffers = mutable.Map.empty[BufferKey, ResponseTimeRangeBuffer]

  def getResponseTimeRangeBuffers(requestName: Option[String], group: Option[Group])(implicit configuration: GatlingConfiguration): ResponseTimeRangeBuffer =
    responseTimeRangeBuffers.getOrElseUpdate(BufferKey(requestName, group, None), new ResponseTimeRangeBuffer)

  def updateResponseTimeRangeBuffer(record: RequestRecord)(implicit configuration: GatlingConfiguration): Unit = {
    import record._
    getResponseTimeRangeBuffers(Some(name), group).update(responseTime, status)
    getResponseTimeRangeBuffers(None, None).update(responseTime, status)
  }

  def updateGroupResponseTimeRangeBuffer(record: GroupRecord)(implicit configuration: GatlingConfiguration): Unit =
    getResponseTimeRangeBuffers(None, Some(record.group)).update(record.duration, record.status)

  class ResponseTimeRangeBuffer(implicit configuration: GatlingConfiguration) {

    var low = 0
    var middle = 0
    var high = 0
    var ko = 0

    def update(time: Int, status: Status): Unit = {

      if (status == KO) ko += 1
      else if (time < configuration.charting.indicators.lowerBound) low += 1
      else if (time > configuration.charting.indicators.higherBound) high += 1
      else middle += 1
    }
  }
}
