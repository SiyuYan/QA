import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class stackOverFlow extends Simulation {

  val httpProtocol = http
    .baseURL("http://stackoverflow.com/")
    .inferHtmlResources(BlackList(""".*\.js.*""", """.*\.css.*""", """.*\.jpg.*""", """.*\.png.*""", """.*\.ico.*""", """.*\.net.*""", """.*\.jpeg.*""", """.*\.gif.*""",""".*\?s=.*"""), WhiteList())
    .acceptHeader("*/*")
    .acceptEncodingHeader("gzip, deflate, sdch")
    .acceptLanguageHeader("en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:45.0) Gecko/20100101 Firefox/45.0")

  val headers_0 = Map(
    "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
    "Upgrade-Insecure-Requests" -> "1")

  val headers_1 = Map(
    "Content-Type" -> "application/x-www-form-urlencoded",
    "X-Requested-With" -> "XMLHttpRequest",
    "Accept" -> "text/html, */*; q=0.01")

  val scn = scenario("stackFlowDemo")
    //Home
    .exec(http("homepage")
    .get("/")
    .headers(headers_0)
  )
    //Search
    .pause(1)
    .exec(http("Search")
      .get("/search")
      .headers(headers_0)
      .queryParam("q", "gatling")
    )
    .pause(1)
    .exec(http("Select")
      .get("/questions/22563517/using-gatling-as-an-integration-test-tool")
      .headers(headers_0)
      .queryParam("s", "1|3.1596")
    )
    .pause(1)

    //Pages
    .exec(http("Page 2")
    .get("/search")
    .headers(headers_0)
    .queryParam("page", "2")
    .queryParam("tab", "relevance")
    .queryParam("q", "gatling")
  )
    .pause(2)
    .exec(http("Page 3")
      .get("/search")
      .headers(headers_0)
      .queryParam("page", "3")
      .queryParam("tab", "relevance")
      .queryParam("q", "gatling")
    )
    .pause(1)
    //Documentations
    .exec(http("Documentations")
    .get("/documentation")
    .headers(headers_0)
  )
    .pause(2)
    .exec(http("Tag")
      .post("/documentation/filter/submit")
      .headers(headers_1)
      .formParam("filter", "CSS")
      .formParam("fkey", "f757bad658420c9ff22bd7a93654132c")
      .formParam("tab", "popular")
    )
    .pause(2)
    .exec(http("Topic")
      .get("/documentation/css/topics")
      .headers(headers_0)
    )

  setUp(scn.inject(rampUsers(5) over (5 seconds))).protocols(httpProtocol)
}