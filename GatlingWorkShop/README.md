# Gatling Workshop

Performance test using Gatling 

## Preparation
Download Zip bundle 

1. http://gatling.io/#/resources/download

## Workshop Steps

### Scenario 
    
    编写脚本,在"stackoverflow"网站上实现如下操作:
    - 进入首页
    - 搜索"gatling",并进入一条搜索结果
    - 在结果列表进行翻页操作
    - 进入"Documentation",根据输入的tag,进入指定主题页面

#### Section 1

1. 访问"stackoverflow" homepage
2. 定义用户注入方式 - rampUsers(nbUsers) over(duration)

##### Refer to 

- http://gatling.io/docs/2.2.2/general/simulation_structure.html#headers-definition
- http://gatling.io/docs/2.0.0-RC2/http/http_request.html#common-parameters
- http://gatling.io/docs/2.0.0-RC2/general/simulation_setup.html#injection

##### Example

     val headers_0 = Map(
         "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
         "Upgrade-Insecure-Requests" -> "1")
         
     val scn = scenario("homepageDemo")
        //Home
        .exec(http("homepage")
        .get("/")
        .headers(headers_0)
      )
      
      setUp(scn.inject(rampUsers(5) over (5 seconds))).protocols(httpProtocol)
      
##### Practice

    仿照上述例子,自己编写对github网站的访问

#### Section 2-1
1. 将三个scenario封装成object
2. 定义三类虚拟用户进行不同操作:userA-search&browse,userB-documentation,userC-search&browse&documentation
3. 使用三种方法丰富用户注入
   - userA: rampUsers(nbUsers) over(duration)
   - userB: atOnceUsers(nbUsers)
   - userC: constantUsersPerSec(rate) during(duration)

##### Refer to
http://gatling.io/docs/2.0.0-RC2/advanced_tutorial.html#step-01-isolate-processes
##### Example

    //Search
    object Search {
        val search =
          exec(http("Home")
            .get("/")
            .headers(headers_0))
            .pause(1)
            .exec(http("Search")
              .get("/search")
              .headers(headers_0)
              .queryParam("q", "gatling"))
            .pause(1)
            .exec(http("Select")
              .get("/questions/22563517/using-gatling-as-an-integration-test-tool")
              .headers(headers_0)
              .queryParam("s", "1|3.1596"))
            .pause(1)
      }
      
      //Browse
      object Browse {
         
         val browse =
            exec(http("Page 2")
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
        }
        
        //Documentation
        object Documentations {
        
            val documentation =
              exec(http("Documentation")
                .get("/documentation")
                .headers(headers_0))
                .pause(1)
                .exec(http("Tag")
                  .post("/documentation/filter/submit")
                  .headers(headers_1)
                  .formParam("filter", "CSS")
                  .formParam("fkey", "f757bad658420c9ff22bd7a93654132c")
                  .formParam("tab", "popular")
                )
                .pause(1)
                .exec(http("Topics")
                  .get("/documentation/css/topics")
                  .headers(headers_0))
          }
          
        val userA = scenario("searchChannel").exec(Search.search, Browse.browse)
        val userB = scenario("documentationsChannel").exec(Documentations.documentation)
        val userC = scenario("allChannel").exec(Search.search, Browse.browse, Documentations.documentation)
        
        setUp(
            userA.inject(rampUsers(5) over (5 seconds)),
            userB.inject(atOnceUsers(10)),
            userC.inject(constantUsersPerSec(5) during (2 seconds))
          ).protocols(httpProtocol)
          


#### Section 2-2
1. 使用feeder中的csv动态传递参数,传入搜索关键字"gatling"
2. 使用check抓取并保存进入Documentation主题页面的URL,检查请求状态为200
3. 访问保存的URL进入指定页面

##### Refer to
- http://gatling.io/docs/2.0.0-RC2/advanced_tutorial.html#step-03-use-dynamic-data-with-feeders-and-checks
- http://gatling.io/docs/2.0.0-RC2/session/feeder.html#feeder
- http://gatling.io/docs/2.0.0-RC2/http/http_check.html#http-response-body
- http://gatling.io/docs/2.0.0-RC2/http/http_check.html#saving

##### Example

    val feeder = csv("keyWords.csv").circular
    //Search
    object Search {
        val search =
          exec(http("Home")
            .get("/")
            .headers(headers_0))
            .pause(1)
            .feed(feeder)
            .exec(http("Search")
              .get("/search")
              .headers(headers_0)
              .queryParam("q", "${searchCriterion}")
            )
            .pause(1)
            .exec(http("Select")
              .get("/questions/22563517/using-gatling-as-an-integration-test-tool")
              .headers(headers_0)
              .queryParam("s", "1|3.1596"))
            .pause(1)
      }
      
      //Browse
      object Browse {
         
         val browse =
            exec(http("Page 2")
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
        }
        
        //Documentation
        object Documentations {
        
            val documentation =
              exec(http("Documentation")
                .get("/documentation")
                .headers(headers_0))
                .pause(1)
                .exec(http("Tag")
                  .post("/documentation/filter/submit")
                  .headers(headers_1)
                  .formParam("filter", "CSS")
                  .formParam("fkey", "f757bad658420c9ff22bd7a93654132c")
                  .formParam("tab", "popular")
                  .check(css("a:contains('CSS')", "href").saveAs("topicsURL"))
                  .check(status.is(200)))
                .pause(1)
                .exec(http("Topics")
                  .get("${topicsURL}")
                  .headers(headers_0))
          }
          
         

#### Homework :)
1. 使用repeat简化翻页

##### Refer to 
- http://gatling.io/docs/2.0.0-RC2/general/scenario.html#repeat




