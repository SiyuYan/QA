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
package io.gatling.core.json

import java.io.{ InputStream, InputStreamReader }
import java.nio.charset.Charset

import io.advantageous.boon.json.implementation.{ JsonFastParser, JsonParserUsingCharacterSource }

class Boon extends JsonParser {

  private def newFastParser = new JsonFastParser(false, false, true, false)

  def parse(bytes: Array[Byte], charset: Charset) = {
    val parser = newFastParser
    parser.setCharset(charset)
    parser.parse(bytes)
  }

  def parse(string: String) =
    newFastParser.parse(string)

  def parse(stream: InputStream, charset: Charset) =
    new JsonParserUsingCharacterSource().parse(new InputStreamReader(stream, charset))
}
