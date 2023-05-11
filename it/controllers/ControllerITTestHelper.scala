package controllers

import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import play.api.http.HeaderNames
import play.api.libs.json.JsValue
import play.api.libs.ws.{DefaultWSCookie, WSClient, WSResponse}
import play.api.test.WsTestClient
import testSupport.{ITCoreTestData, Specifications, TestConfiguration}

import scala.concurrent.Future

trait ControllerITTestHelper extends Specifications with TestConfiguration with ITCoreTestData {

  def createClientRequestGet(client: WSClient, url: String): Future[WSResponse] = {
    client.url(url)
      .withFollowRedirects(false)
      .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
      .get()
  }

  def createClientRequestPOST(client: WSClient, url: String, json: JsValue): Future[WSResponse] = {
    client.url(url)
      .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
      .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
        "Csrf-Token" -> "nocheck")
      .withFollowRedirects(false)
      .post(json)
  }

  def testUnauthorisedUserGET(url: String): Unit = {
    "the user is unauthenticated" - {
      "redirect to unauthorised controller" in {
        given.unauthorisedPrecondition

        WsTestClient.withClient { client =>
          val result1 = client.url(url)
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).get must include("/auth-login-stub/gg-sign-in")
          }
        }
      }
    }
  }

  def testUnauthorisedUserPOST(url: String, json: JsValue): Unit = {
    "the user is unauthenticated" - {
      "redirect to gg-signin" in {
        given.unauthorisedPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(url)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022",
              "Csrf-Token" -> "nocheck")
            .withFollowRedirects(false)
            .post(json)

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).get must include("/auth-login-stub/gg-sign-in")
          }
        }
      }
    }
  }
}

