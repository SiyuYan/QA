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
package io.gatling.recorder.har

import java.io.{ FileInputStream, InputStream }
import java.net.URL

import scala.collection.breakOut
import scala.util.Try

import io.gatling.commons.util.Io._
import io.gatling.http.HeaderNames._
import io.gatling.http.fetch.HtmlParser
import io.gatling.recorder.config.RecorderConfiguration
import io.gatling.recorder.scenario._

import org.asynchttpclient.uri.Uri
import io.netty.handler.codec.http.HttpMethod

/**
 * Implementation according to http://www.softwareishard.com/blog/har-12-spec/
 */
private[recorder] object HarReader {

  def apply(path: String)(implicit config: RecorderConfiguration): ScenarioDefinition =
    withCloseable(new FileInputStream(path))(apply(_))

  def apply(jsonStream: InputStream)(implicit config: RecorderConfiguration): ScenarioDefinition =
    apply(Json.parseJson(jsonStream))

  private def apply(json: Json)(implicit config: RecorderConfiguration): ScenarioDefinition = {
    val HttpArchive(Log(entries)) = HarMapping.jsonToHttpArchive(json)

    val elements = entries.iterator
      .filter(e => e.request.method != HttpMethod.CONNECT.name)
      .filter(e => isValidURL(e.request.url))
      // TODO NICO : can't we move this in Scenario as well ?
      .filter(e => config.filters.filters.map(_.accept(e.request.url)).getOrElse(true))
      .map(createRequestWithArrivalTime)
      .toVector

    ScenarioDefinition(elements, Nil)
  }

  private def createRequestWithArrivalTime(entry: Entry): TimedScenarioElement[RequestElement] = {
      def buildContent(postParams: Seq[PostParam]): RequestBody =
        RequestBodyParams(postParams.map(postParam => (postParam.name, postParam.value)).toList)

    val uri = entry.request.url
    val method = entry.request.method
    val requestHeaders = buildRequestHeaders(entry)

    // NetExport doesn't copy post params to text field
    val requestBody = entry.request.postData.flatMap { postData =>
      if (postData.params.nonEmpty)
        Some(buildContent(postData.params))
      else
        postData.textAsBytes map RequestBodyBytes
    }

    val responseBody = entry.response.content.textAsBytes map ResponseBodyBytes

    val embeddedResources = entry.response.content match {
      case Content("text/html", Some(text)) =>
        val userAgent = requestHeaders.get(UserAgent).flatMap(io.gatling.http.fetch.UserAgent.parseFromHeader)
        new HtmlParser().getEmbeddedResources(Uri.create(uri), text, userAgent)
      case _ => Nil
    }

    TimedScenarioElement(entry.sendTime, entry.sendTime, RequestElement(uri, method, requestHeaders, requestBody, responseBody, entry.response.status, embeddedResources))
  }

  private def buildRequestHeaders(entry: Entry): Map[String, String] = {
    // Chrome adds extra headers, eg: ":host". We should have them in the Gatling scenario.
    val headers: Map[String, String] = entry.request.headers.filter(!_.name.startsWith(":")).map(h => (h.name, h.value))(breakOut)

    // NetExport doesn't add Content-Type to headers when POSTing, but both Chrome Dev Tools and NetExport set mimeType
    entry.request.postData.map(postData => headers.updated(ContentType, postData.mimeType)).getOrElse(headers)
  }

  private def isValidURL(url: String): Boolean = Try(new URL(url)).isSuccess
}
