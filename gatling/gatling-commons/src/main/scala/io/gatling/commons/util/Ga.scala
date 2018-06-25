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
package io.gatling.commons.util

import java.net.{ URL, URLEncoder }
import java.nio.charset.StandardCharsets.UTF_8
import java.util.UUID
import javax.net.ssl.HttpsURLConnection

import scala.util.Properties._
import scala.util.Try

import Io._

object Ga {

  def send(version: String): Unit =
    Try {

      val url = new URL("https://ssl.google-analytics.com/collect")

      val conn = url.openConnection().asInstanceOf[HttpsURLConnection]

      try {
        conn.connect()
        conn.setReadTimeout(2000)
        conn.setConnectTimeout(2000)
        conn.setRequestMethod("POST")
        conn.setRequestProperty("Connection", "Close")
        conn.setRequestProperty("User-Agent", s"java/$javaVersion")
        conn.setUseCaches(false)

        withCloseable(conn.getOutputStream) { os =>

          val trackingId = if (version.endsWith("SNAPSHOT")) "UA-53375088-4" else "UA-53375088-5"

            def encode(string: String) = URLEncoder.encode(string, UTF_8.name)

          val body =
            s"""tid=$trackingId&
              |dl=${encode("http://gatling.io/" + version)}&
              |de=UTF-8}&
              |ul=en-US}&
              |t=pageview&
              |dt=${encode(version)}&
              |cid=${encode(UUID.randomUUID.toString)}""".stripMargin

          os.write(body.getBytes(UTF_8))
          os.flush()
        }
      } finally {
        conn.disconnect()
      }
    }
}
