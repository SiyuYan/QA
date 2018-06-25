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
package io.gatling.charts.report

import io.gatling.charts.component.ComponentLibrary
import io.gatling.charts.config.ChartsFiles.allSessionsFile
import io.gatling.charts.util.Colors.Orange
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.stats.{ IntVsTimePlot, Series }

private[charts] class AllSessionsReportGenerator(reportsGenerationInputs: ReportsGenerationInputs, componentLibrary: ComponentLibrary)(implicit configuration: GatlingConfiguration)
    extends ReportGenerator {

  def generate(): Unit = {
    import reportsGenerationInputs._

    val series = new Series[IntVsTimePlot]("All Users", dataReader.numberOfActiveSessionsPerSecond(None), List(Orange))

    val javascript = componentLibrary.getAllUsersJs(dataReader.runStart, series)

    new TemplateWriter(allSessionsFile(reportFolderName)).writeToFile(javascript)
  }
}
