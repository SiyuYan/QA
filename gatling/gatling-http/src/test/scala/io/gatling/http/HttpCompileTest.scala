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
package io.gatling.http

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class HttpCompileTest extends Simulation {

  val iterations = 10
  val pause1 = 1
  val pause2 = 2
  val pause3 = 3
  val pause4 = Integer.getInteger("testProperty")

  val pause5 = pause4 milliseconds
  val pause6 = pause4 seconds
  val pause7 = pause4 nanoseconds

  val baseUrl = "http://localhost:3000"

  val httpProtocol = http
    .baseURL("http://172.30.5.143:8080")
    .proxy(Proxy("172.31.76.106", 8080).httpsPort(8081))
    .noProxyFor("localhost")
    .acceptHeader("*/*")
    .acceptCharsetHeader("ISO-8859-1,utf-8;q=0.7,*;q=0.3")
    .acceptLanguageHeader("fr-FR,fr;q=0.8,en-US;q=0.6,en;q=0.4")
    .acceptEncodingHeader("gzip,deflate,sdch")
    .userAgentHeader("Mozilla/5.0 (X11; Linux i686) AppleWebKit/535.19 (KHTML, like Gecko) Ubuntu/12.04 Chromium/18.0.1025.151 Chrome/18.0.1025.151 Safari/535.19")
    .check(bodyString.transform(string => string.size).lessThan(100000))
    .check(bodyString.transform((string, session) => string.size).lessThan(100000))
    .check(bodyString.transformOption(stringO => stringO.map(_.size)).lessThan(100000))
    .check(bodyString.transformOption((stringO, session) => stringO.map(_.size)).lessThan(100000))
    .check(form("#form").transform { foo: Map[String, Seq[String]] => foo }.saveAs("theForm"))
    .disableCaching
    .disableWarmUp
    .warmUp("http://gatling.io")
    .inferHtmlResources(white = WhiteList(".*\\.html"))

  val httpConfToVerifyDumpSessionOnFailureBuiltIn = http.extraInfoExtractor(dumpSessionOnFailure)

  val httpConfToVerifyUserProvidedInfoExtractors = http
    .extraInfoExtractor(extraInfo => List(extraInfo.requestName, extraInfo.response.body.string))

  val usersInformation = tsv("user_information.tsv")

  val loginChain = exec(http("First Request Chain").get("/")).pause(1)

  val testData = tsv("test-data.tsv")

  val richTestData = testData.convert { case ("keyOfAMultivaluedColumn", value) => value.split(",") }

  val testData3 = Array(Map("foo" -> "bar")).circular

  val lambdaUser = scenario("Standard User")
    .exec(loginChain)
    // First request outside iteration
    .repeat(2) {
      feed(richTestData)
        .exec(http("Catégorie Poney").get("/").queryParam("omg", "${omg}").queryParam("socool", "${socool}").basicAuth("", "").check(xpath("//input[@id='text1']/@value").transform(_.map(_ + "foo")).saveAs("aaaa_value"), jsonPath("//foo/bar[2]/baz")))
    }
    .repeat(2, "counterName") {
      feed(testData.circular)
        .exec(http("Catégorie Poney").get("/").queryParam("omg", "${omg}").queryParam("socool", "${socool}").basicAuth("", "").check(xpath("//input[@id='text1']/@value").findAll.saveAs("aaaa_value"), jsonPath("//foo/bar[2]/baz")))
    }
    .during(10 seconds) {
      feed(testData)
        .exec(http("Catégorie Poney").get("/").queryParam("omg", "${omg}").queryParam("socool", "${socool}").basicAuth("", "").check(xpath("//input[@id='text1']/@value").saveAs("aaaa_value"), jsonPath("//foo/bar[2]/baz")))
    }
    .forever {
      feed(testData)
        .exec(http("Catégorie Poney").get("/").queryParam("omg", "${omg}").queryParam("socool", "${socool}").basicAuth("", "").check(xpath("//input[@id='text1']/@value").saveAs("aaaa_value"), jsonPath("//foo/bar[2]/baz")))
    }
    .group("C'est ici qu'on trouve des Poneys") {
      exec(http("Catégorie Poney").post("/")
        .form("${theForm}")
        .formParam("baz", "${qix}")
        .multivaluedFormParam("foo", Seq("bar")))
        .exec(http("Catégorie Poney").post("/").multivaluedFormParam("foo", "${bar}"))
        .exec(http("Catégorie Poney").get("/").queryParam("omg", "foo"))
        .exec(http("Catégorie Poney").get("/").queryParam("omg", "${foo}"))
        .exec(http("Catégorie Poney").get("/").queryParam("omg", session => "foo"))
        .exec(http("Catégorie Poney").get("/").multivaluedQueryParam("omg", List("foo")))
        .exec(http("Catégorie Poney").get("/").multivaluedQueryParam("omg", "${foo}"))
        .exec(http("Catégorie Poney").get("/").multivaluedQueryParam("omg", List("foo")))
    }
    .exec(http("Catégorie Poney").get("/")
      .resources(
        http("Catégorie Poney").post("/").multivaluedFormParam("foo", "${bar}"),
        http("Catégorie Poney").get("/").queryParam("omg", "foo"),
        http("Catégorie Poney").get("/").queryParam("omg", "${foo}"),
        http("Catégorie Poney").get("/").queryParam("omg", session => "foo")
      ))
    .uniformRandomSwitch(exec(http("Catégorie Poney").get("/")), exec(http("Catégorie Licorne").get("/")))
    .randomSwitch(
      40d -> exec(http("Catégorie Poney").get("/")),
      50d -> exec(http("Catégorie Licorne").get("/"))
    )
    .randomSwitch(40d -> exec(http("Catégorie Poney").get("/")))
    .pause(pause2)
    // Loop
    .repeat(iterations, "titi") {
      // What will be repeated ?
      // First request to be repeated
      exec(session => {
        println("iterate: " + session("titi"))
        session
      })
        .exec(
          http("Page accueil").get("http://localhost:3000")
            .check(
              xpath("//input[@value='${aaaa_value}']/@id").saveAs("sessionParam"),
              xpath("//input[@id='${aaaa_value}']/@value").notExists,
              css(".foo"),
              css("#foo", "href"),
              css(".foo").ofType[Node].count.is(1),
              css(".foo").notExists,
              css("#foo").ofType[Node].transform { node: Node => node.getNodeName },
              regex("""<input id="text1" type="text" value="aaaa" />""").optional.saveAs("var1"),
              regex("""<input id="text1" type="text" value="aaaa" />""").count.is(1),
              regex("""<input id="text1" type="test" value="aaaa" />""").notExists,
              status.in(200 to 210).saveAs("blablaParam"),
              status.in(200, 210).saveAs("blablaParam"),
              status.in(Seq(200, 304)).saveAs("blablaParam"),
              bodyBytes.is(RawFileBody("foobar.txt")),
              bodyString.is(ElFileBody("foobar.txt")),
              xpath("//input[@value='aaaa']/@id").not("omg"),
              xpath("//input[@id='text1']/@value").is("aaaa").saveAs("test2"),
              md5.is("0xA59E79AB53EEF2883D72B8F8398C9AC3"),
              substring("Foo"),
              responseTimeInMillis.lessThan(1000)
            )
        )
        .during(12000 milliseconds, "foo") {
          exec(http("In During 1").get("http://localhost:3000/aaaa"))
            .pause(2, constantPauses)
            .repeat(2, "tutu") {
              exec(session => {
                println("--nested loop: " + session("tutu"))
                session
              })
            }
            .exec(session => {
              println("-loopDuring: " + session("foo"))
              session
            })
            .exec(http("In During 2").get("/"))
            .pause(2)
        }
        .pause(pause2)
        .during(12000 milliseconds, "hehe") {
          exec(http("In During 1").get("/"))
            .pause(2)
            .exec(session => {
              println("-iterate1: " + session("titi") + ", doFor: " + session("hehe"))
              session
            })
            .repeat(2, "hoho") {
              exec(session => {
                println("--iterate1: " + session("titi") + ", doFor: " + session("hehe") + ", iterate2: " + session("hoho"))
                session
              })
            }
            .exec(http("In During 2").get("/"))
            .pause(2)
        }
        .exec(session => session.set("test2", "bbbb"))
        .doIfEqualsOrElse("test2", "aaaa") {
          exec(http("IF=TRUE Request").get("/"))
        } {
          exec(http("IF=FALSE Request").get("/"))
        }.pause(pause2)
        .exec(http("Url from session").get("/aaaa"))
        .pause(1000 milliseconds)
        // Second request to be repeated
        .exec(http("Create Thing blabla").post("/things").queryParam("login", "${login}").queryParam("password", "${password}").body(ElFileBody("create_thing.txt")).asJSON)
        .pause(pause1)
        // Third request to be repeated
        .exec(http("Liste Articles").get("/things").queryParam("firstname", "${firstname}").queryParam("lastname", "${lastname}"))
        .pause(pause1)
        .exec(http("Test Page").get("/tests").check(header(HttpHeaderNames.ContentType).is("text/html; charset=utf-8").saveAs("sessionParam")))
        // Fourth request to be repeated
        .pause(100 milliseconds)
        // switch
        .randomSwitch(
          40d -> exec(http("Possibility 1").get("/p1")),
          55d -> exec(http("Possibility 2").get("/p2")) // last 5% bypass
        )
        .exec(http("Create Thing omgomg")
          .post("/things").queryParam("postTest", "${sessionParam}").body(RawFileBody("create_thing.txt")).asJSON
          .check(status.is(201).saveAs("status")))
    }
    // Head request
    .exec(http("head on root").head("/").proxy(Proxy("172.31.76.106", 8080).httpsPort(8081)))
    // Second request outside iteration
    .exec(http("Ajout au panier").get("/").check(regex("""<input id="text1" type="text" value="(.*)" />""").saveAs("input")))
    .exec(http("Ajout au panier").get("/").check(regex(session => """<input id="text1" type="text" value="smth" />""").count.lessThan(10).saveAs("input")))
    .exec(http("Ajout au panier").get("/").check(regex(session => """<input id="(.*)" type="text" value="(.*)" />""").ofType[(String, String)] saveAs ("input")))
    .pause(pause1)
    .exec(polling.every(10.seconds).exec(http("poll").get("/foo")))
    .exec(polling.pollerName("poll").every(10.seconds).exec(http("poll").get("/foo")))
    .exec(polling.pollerName("poll").stop)
    .exec(polling.stop)
    .rendezVous(100)
    .exec(flushSessionCookies)
    .pause(pause4)
    .exec(addCookie(Cookie("foo", "bar").withDomain("/")))
    .pace(5)
    .pace(5 seconds)
    .pace("${foo}")
    .pace(5, 10)
    .pace("${foo}", "${bar}")
    .doSwitch("${foo}")(
      "a" -> exec(http("a").get("/")),
      "b" -> exec(http("b").get("/"))
    )
    .doSwitchOrElse("${foo}")(
      "a" -> exec(http("a").get("/")),
      "b" -> exec(http("b").get("/")) //
    )(exec(http("else").get("/")))

  val inject1 = nothingFor(10 milliseconds)
  val inject2 = rampUsers(10).over(10 minutes)
  val inject3 = constantUsersPerSec(10).during(1 minute)
  val inject4 = atOnceUsers(100)
  val inject5 = rampUsersPerSec(10) to (20) during (10 minutes)
  val inject6 = splitUsers(1000).into(rampUsers(10) over (10 seconds)).separatedBy(10 seconds)
  val inject7 = splitUsers(1000).into(rampUsers(10) over (10 seconds)).separatedBy(atOnceUsers(30))
  val inject8 = heavisideUsers(1000) over (20 seconds)

  val injectionSeq = Vector(1, 2, 4, 8).map(x => rampUsers(x * 100) over (5 seconds))
  setUp(
    lambdaUser.inject(inject1),
    lambdaUser.inject(injectionSeq: _*),
    lambdaUser.inject(inject1, inject2).throttle(jumpToRps(20), reachRps(40) in (10 seconds), holdFor(30 seconds))
  )
    .protocols(httpProtocol)
    .pauses(uniformPausesPlusOrMinusPercentage(1))
    .disablePauses
    .constantPauses
    .exponentialPauses
    .uniformPauses(1.5)
    .uniformPauses(1337 seconds)
    .assertions(
      global.responseTime.mean.lessThan(50),
      global.responseTime.max.between(50, 500),
      global.successfulRequests.count.greaterThan(1500),
      global.allRequests.percent.is(100),
      forAll.failedRequests.percent.is(0),
      forAll.responseTime.max.is(100),
      details("Users" / "Search" / "Index page").responseTime.mean.greaterThan(0),
      details("Admins" / "Create").failedRequests.percent.lessThan(90),
      details("request_9").requestsPerSec.greaterThan(10)
    )
    .throttle(jumpToRps(20), reachRps(40) in (10 seconds), holdFor(30 seconds))
    // Applies on the setup
    .constantPauses
    .disablePauses
    .exponentialPauses
    .uniformPauses(1.5)
    .uniformPauses(1337 seconds)
}
