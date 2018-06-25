import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class homepage extends Simulation {

  //http protocol definition(baseUrl and common headers)
  val httpProtocol = http
    .baseURL("http://stackoverflow.com/")
    .inferHtmlResources(BlackList(""".*\.js.*""", """.*\.css.*""", """.*\.jpg.*""", """.*\.png.*""", """.*\.ico.*""", """.*\.net.*""", """.*\.jpeg.*""", """.*\.gif.*""",""".*\?s=.*"""), WhiteList())
    .acceptHeader("*/*")
    .acceptEncodingHeader("gzip, deflate, sdch")
    .acceptLanguageHeader("en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:45.0) Gecko/20100101 Firefox/45.0")

    //headers definition(for request)

    //scenario definition(access homepage)

    //simulation definition(inject virtual users)

}