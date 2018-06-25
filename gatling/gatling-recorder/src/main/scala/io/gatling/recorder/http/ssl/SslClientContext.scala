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
package io.gatling.recorder.http.ssl

import javax.net.ssl.{ SSLEngine, SSLContext }

private[http] object SslClientContext {

  val SslContext = {
    val clientContext = SSLContext.getInstance(SslServerContext.Protocol)
    clientContext.init(null, TrustManagerFactory.LooseTrustManagers, null)
    clientContext
  }

  def createSSLEngine: SSLEngine = {
    val engine = SslContext.createSSLEngine
    engine.setUseClientMode(true)
    engine
  }
}
