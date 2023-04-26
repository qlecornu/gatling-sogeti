package sogeti

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class OpencruiseSimulation extends Simulation {

  val httpProtocol =
    http.baseUrl("https://opencruise-perf.sogeti-center.cloud/")
      .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
      .acceptLanguageHeader("en-US,en;q=0.5")
      .acceptEncodingHeader("gzip, deflate")
      .userAgentHeader(
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0"
      )

  val authorizationHeader = Map(
    "Authorization" -> "Bearer #{access_token}"
  )

  val feeder = csv("credential.csv").random

  val browse = scenario("Load test demo")
    .exec(
      http("access login page")
        .get("login")
    )
    .pause(1)
    .feed(feeder)
    .exec(
      http("signin")
        .post("api/users/signin")
        .formParam("username", "#{username}")
        .formParam("password", "#{password}")
        .check(
          status.is(200),
          jmesPath("token").saveAs("access_token")
        )
    )
    .exec(
      http("api/home/croisiereByContinent/europe")
        .get("api/home/croisiereByContinent/europe")
        .headers(authorizationHeader)
    )
    .exec(
      http("api/home/randomCroisiere")
        .get("api/home/randomCroisiere")
        .headers(authorizationHeader)
    )

  private val USERS = System.getProperty("USERS", "1").toInt;

  setUp(
    browse.inject(atOnceUsers(USERS))
  ).protocols(httpProtocol)

}
