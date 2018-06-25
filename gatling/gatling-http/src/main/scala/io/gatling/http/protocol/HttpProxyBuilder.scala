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
package io.gatling.http.protocol

import io.gatling.core.config.Credentials

object HttpProxyBuilder {

  def apply(host: String, port: Int) = new HttpProxyBuilder(Proxy(host, port, port))

  implicit def toProxy(proxyBuilder: HttpProxyBuilder): Proxy = proxyBuilder.proxy
}

class HttpProxyBuilder(val proxy: Proxy) {

  def httpsPort(port: Int) = new HttpProxyBuilder(proxy.copy(securePort = port))

  def credentials(username: String, password: String) = new HttpProxyBuilder(proxy.copy(credentials = Some(Credentials(username, password))))
}
