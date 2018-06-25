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
package io.gatling.charts.component

import io.gatling.commons.stats.ErrorStats
import io.gatling.commons.util.StringHelper.EmptyFastring
import io.gatling.commons.util.HtmlHelper.HtmlRichString
import io.gatling.commons.util.NumberHelper._

import com.dongxiguo.fastring.Fastring.Implicits._

private[charts] class ErrorsTableComponent(errors: Seq[ErrorStats]) extends Component {

  def js = fast"""
	    $$('#container_errors').sortable('#container_errors');
    """

  def html = if (errors.isEmpty)
    EmptyFastring
  else
    fast"""<div class="statistics extensible-geant collapsed">
    <div class="title">
        <div class="title_collapsed" style="cursor: auto;">ERRORS</div>
    </div>
    <table id="container_errors" class="statistics-in extensible-geant">
        <thead>
            <tr>
                <th id="error-col-1" class="header sortable"><span>Error</span></th>
                <th id="error-col-2" class="header sortable"><span>Count</span></th>
                <th id="error-col-3" class="header sortable"><span>Percentage</span></th>
            </tr>
        </thead>
		<tbody>
		    ${
      errors.zipWithIndex.map {
        case (error, index) => fast"""
		    <tr>
		    	<td class="error-col-1 total">${error.message.htmlEscape}<span class="value" style="display:none">$index</span></td>
		    	<td class="value error-col-2 total">${error.count}</td>
		    	<td class="value error-col-3 total">${error.percentage.toPrintableString} %</td>
		    </tr>"""
      }.mkFastring
    }
		</tbody>
    </table>
</div>
"""

  def jsFiles: Seq[String] = Seq.empty
}
