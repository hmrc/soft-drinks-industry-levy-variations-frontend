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

  def testUnauthorisedUser(url: String, optJson: Option[JsValue] = None): Unit = {
    "the user is unauthenticated" - {
      "redirect to gg-signin" in {
        given.unauthorisedPrecondition

        WsTestClient.withClient { client =>
          val result1 = optJson match {
            case Some(json) => createClientRequestPOST(client, url, json)
            case _ => createClientRequestGet(client, url)
          }

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).get must include("/bas-gateway/sign-in")
          }
        }
      }
    }

    "the user is authenticated but has no enrolments" - {
      "redirect to unauthorised controller" in {
        given.authorisedButNoEnrolmentsPrecondition

        WsTestClient.withClient { client =>
          val result1 = optJson match {
            case Some(json) => createClientRequestPOST(client, url, json)
            case _ => createClientRequestGet(client, url)
          }

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).get must include("/unauthorised")
          }
        }
      }
    }
    "the user is authenticated but has no sdil subscription" - {
      "redirect to unauthorised controller" in {
        given.authorisedButNoSdilSubscriptionPrecondition

        WsTestClient.withClient { client =>
          val result1 = optJson match {
            case Some(json) => createClientRequestPOST(client, url, json)
            case _ => createClientRequestGet(client, url)
          }

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).get must include("/unauthorised")
          }
        }
      }
    }
  }

  def testAuthenticatedUserButNoUserAnswers(url: String, optJson: Option[JsValue] = None): Unit = {
    "the user is authenticated but has no sdil subscription" - {
      "redirect to journey recover controller" in {
        given.commonPrecondition

        remove(sdilNumber)

        WsTestClient.withClient { client =>
          val result1 = optJson match {
            case Some(json) => createClientRequestPOST(client, url, json)
            case _ => createClientRequestGet(client, url)
          }

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).get must include("/there-is-a-problem")
          }
        }
      }
    }
  }
}

