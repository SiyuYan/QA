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
package io.gatling.metrics.sender

import java.net.InetSocketAddress

import io.gatling.metrics.message.GraphiteMetrics

import akka.actor.ActorRef
import akka.io.{ IO, Udp }

private[metrics] class UdpSender(remote: InetSocketAddress) extends MetricsSender {

  import Udp._

  IO(Udp) ! SimpleSender

  def receive = uninitialized

  private def uninitialized: Receive = {
    case SimpleSenderReady =>
      unstashAll()
      context become connected(sender())
    case _ => stash()
  }

  private def connected(connection: ActorRef): Receive = {
    case GraphiteMetrics(bytes) => connection ! Send(bytes, remote)
  }
}
