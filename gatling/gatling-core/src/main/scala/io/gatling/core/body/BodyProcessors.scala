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
package io.gatling.core.body

import java.io.FileInputStream

import io.gatling.commons.util.{ GzipHelper, FastByteArrayInputStream }
import io.gatling.commons.util.Io._
import io.gatling.core.config.GatlingConfiguration

object BodyProcessors {

  def gzip(implicit configuration: GatlingConfiguration) = (body: Body) => {

    val gzippedBytes = body match {
      case StringBody(string)       => string.map(GzipHelper.gzip)
      case ByteArrayBody(byteArray) => byteArray.map(GzipHelper.gzip)
      case RawFileBody(fileWithCachedBytes) => fileWithCachedBytes.map { f =>
        f.cachedBytes match {
          case Some(bytes) => GzipHelper.gzip(bytes)
          case None        => withCloseable(new FileInputStream(f.file))(GzipHelper.gzip(_))
        }
      }
      case InputStreamBody(inputStream) => inputStream.map(withCloseable(_)(GzipHelper.gzip(_)))
      case _                            => throw new UnsupportedOperationException(s"requestCompressor doesn't support $body")
    }

    ByteArrayBody(gzippedBytes)
  }

  def stream(implicit configuration: GatlingConfiguration) = (body: Body) => {

    val stream = body match {
      case stringBody: StringBody   => stringBody.asBytes.bytes.map(new FastByteArrayInputStream(_))
      case ByteArrayBody(byteArray) => byteArray.map(new FastByteArrayInputStream(_))
      case RawFileBody(fileWithCachedBytes) => fileWithCachedBytes.map { f =>
        f.cachedBytes match {
          case Some(bytes) => new FastByteArrayInputStream(bytes)
          case None        => new FileInputStream(f.file)
        }
      }
      case InputStreamBody(inputStream) => inputStream
      case _                            => throw new UnsupportedOperationException(s"streamBody doesn't support $body")
    }

    InputStreamBody(stream)
  }
}
