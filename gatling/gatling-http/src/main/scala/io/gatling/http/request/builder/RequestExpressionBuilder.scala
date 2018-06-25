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
package io.gatling.http.request.builder

import java.net.InetAddress

import scala.util.control.NonFatal

import io.gatling.commons.validation._
import io.gatling.core.session.{ Expression, Session }
import io.gatling.http.HeaderNames
import io.gatling.http.ahc.{ AhcRequestBuilder, ChannelPoolPartitioning }
import io.gatling.http.cache.HttpCaches
import io.gatling.http.cookie.CookieSupport
import io.gatling.http.protocol.HttpComponents
import io.gatling.http.referer.RefererHandling
import io.gatling.http.util.DnsHelper

import com.typesafe.scalalogging.LazyLogging
import org.asynchttpclient.channel.{ NameResolution, NameResolver }
import org.asynchttpclient.Request
import org.asynchttpclient.uri.Uri

object RequestExpressionBuilder {
  val BuildRequestErrorMapper = "Failed to build request: " + _

  val LoopbackAddress = Array(new NameResolution(InetAddress.getLoopbackAddress, Long.MaxValue))
}

abstract class RequestExpressionBuilder(commonAttributes: CommonAttributes, httpComponents: HttpComponents)
    extends LazyLogging {

  import RequestExpressionBuilder._
  val protocol = httpComponents.httpProtocol
  val httpCaches = httpComponents.httpCaches
  protected val charset = httpComponents.httpEngine.configuration.core.charset

  def makeAbsolute(url: String): Validation[Uri] =
    protocol.makeAbsoluteHttpUri(url)

  def buildURI(session: Session): Validation[Uri] =
    commonAttributes.urlOrURI match {
      case Left(url) =>
        try {
          url(session).flatMap(makeAbsolute)
        } catch {
          // don't use safe in order to save lambda instances
          case NonFatal(e) => s"url $url can't be parsed into a URI: ${e.getMessage}".failure
        }
      case Right(uri) => uri.success
    }

  def configureAddressNameResolver(session: Session, httpCaches: HttpCaches)(requestBuilder: AhcRequestBuilder): AhcRequestBuilder = {
    if (!protocol.enginePart.shareDnsCache) {
      requestBuilder.setNameResolver(new NameResolver {
        override def resolve(name: String): Array[NameResolution] = name match {
          case "localhost" => LoopbackAddress
          case _ =>
            httpCaches.dnsLookupCacheEntry(session, name) match {
              case Some(addresses) => addresses
              case None =>
                try {
                  DnsHelper.getAddressesByName(name)
                } catch {
                  case NonFatal(e) =>
                    logger.warn(s"Failed to resolve address of name $name")
                    NameResolver.JdkNameResolver.INSTANCE.resolve(name)
                }
            }
        }
      })
    }
    requestBuilder
  }

  def configureProxy(requestBuilder: AhcRequestBuilder, uri: Uri): Validation[AhcRequestBuilder] = {
    if (!protocol.proxyPart.proxyExceptions.contains(uri.getHost)) {
      val proxy = commonAttributes.proxy.orElse(protocol.proxyPart.proxy)
      proxy.foreach(requestBuilder.setProxyServer)
    }
    requestBuilder.success
  }

  def configureCookies(session: Session, uri: Uri)(requestBuilder: AhcRequestBuilder): AhcRequestBuilder = {
    CookieSupport.getStoredCookies(session, uri).foreach(requestBuilder.addCookie)
    requestBuilder
  }

  def configureQuery(session: Session, uri: Uri)(requestBuilder: AhcRequestBuilder): Validation[AhcRequestBuilder] =
    commonAttributes.queryParams match {
      case Nil         => requestBuilder.success
      case queryParams => queryParams.resolveParamJList(session).map(requestBuilder.addQueryParams)
    }

  def configureVirtualHost(session: Session)(requestBuilder: AhcRequestBuilder): Validation[AhcRequestBuilder] =
    commonAttributes.virtualHost.orElse(protocol.enginePart.virtualHost) match {
      case None              => requestBuilder.success
      case Some(virtualHost) => virtualHost(session).map(requestBuilder.setVirtualHost)
    }

  def configureHeaders(session: Session)(requestBuilder: AhcRequestBuilder): Validation[AhcRequestBuilder] = {

    val headers = protocol.requestPart.headers ++ commonAttributes.headers

    val requestBuilderWithHeaders = headers.foldLeft(requestBuilder.success) { (requestBuilder, header) =>
      val (key, value) = header
      for {
        requestBuilder <- requestBuilder
        value <- value(session)
      } yield requestBuilder.addHeader(key, value)
    }

    val additionalRefererHeader =
      if (headers.contains(HeaderNames.Referer))
        None
      else
        RefererHandling.getStoredReferer(session)

    additionalRefererHeader match {
      case Some(referer) => requestBuilderWithHeaders.map(_.addHeader(HeaderNames.Referer, referer))
      case _             => requestBuilderWithHeaders
    }
  }

  def configureRealm(session: Session)(requestBuilder: AhcRequestBuilder): Validation[AhcRequestBuilder] =
    commonAttributes.realm.orElse(protocol.requestPart.realm) match {
      case Some(realm) => realm(session).map(requestBuilder.setRealm)
      case None        => requestBuilder.success
    }

  def configureLocalAddress(session: Session)(requestBuilder: AhcRequestBuilder): Validation[AhcRequestBuilder] =
    commonAttributes.address.orElse(protocol.enginePart.localAddress) match {
      case Some(localAddress) => localAddress(session).map(requestBuilder.setLocalInetAddress)
      case None               => requestBuilder.success
    }

  protected def configureRequestBuilder(session: Session, uri: Uri, requestBuilder: AhcRequestBuilder): Validation[AhcRequestBuilder] =
    configureProxy(requestBuilder.setUri(uri), uri)
      .map(configureAddressNameResolver(session, httpCaches))
      .map(configureCookies(session, uri))
      .flatMap(configureQuery(session, uri))
      .flatMap(configureVirtualHost(session))
      .flatMap(configureHeaders(session))
      .flatMap(configureRealm(session))
      .flatMap(configureLocalAddress(session))

  def build: Expression[Request] = {

    val disableUrlEncoding = commonAttributes.disableUrlEncoding.getOrElse(protocol.requestPart.disableUrlEncoding)

    (session: Session) => {
      val requestBuilder = new AhcRequestBuilder(commonAttributes.method, disableUrlEncoding)

      requestBuilder.setBodyCharset(charset)

      if (!protocol.enginePart.shareConnections)
        requestBuilder.setConnectionPoolPartitioning(new ChannelPoolPartitioning(session))

      safely(BuildRequestErrorMapper) {
        for {
          uri <- buildURI(session)
          rb <- configureRequestBuilder(session, uri, requestBuilder)
        } yield rb.build
      }
    }
  }
}
