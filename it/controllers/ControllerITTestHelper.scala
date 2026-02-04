package controllers

import models.{ SelectChange, UserAnswers }
import org.scalatest.matchers.must.Matchers._
import play.api.http.HeaderNames
import play.api.libs.json.JsValue
import play.api.libs.ws.{ DefaultWSCookie, WSClient, WSResponse }
import play.api.test.WsTestClient
import testSupport.{ ITCoreTestData, Specifications, TestConfiguration }

import scala.concurrent.Future
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import play.api.libs.ws.DefaultBodyWritables.writeableOf_String

trait ControllerITTestHelper extends Specifications with TestConfiguration with ITCoreTestData {
  def createClientRequestGet(client: WSClient, url: String): Future[WSResponse] =
    client
      .url(url)
      .withFollowRedirects(false)
      .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
      .get()

  def createClientRequestPOST(client: WSClient, url: String, json: JsValue): Future[WSResponse] =
    client
      .url(url)
      .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
      .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022", "Csrf-Token" -> "nocheck")
      .withFollowRedirects(false)
      .post(json)

  def createClientRequestPOSTNoData(client: WSClient, url: String, body: String): Future[WSResponse] =
    client
      .url(url)
      .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
      .withHttpHeaders("X-Session-ID" -> "XKSDIL000000022", "Csrf-Token" -> "nocheck")
      .withFollowRedirects(false)
      .post(body)

  def emptyUserAnswersForSelectChange(selectChange: SelectChange) =
    if (selectChange == SelectChange.CorrectReturn) {
      emptyUserAnswersForCorrectReturn
    } else {
      UserAnswers(sdilNumber, selectChange, contactAddress = ukAddress)
    }

  def testRequiredCorrectReturnDataMissing(url: String, optJson: Option[JsValue] = None): Unit =
    "should redirect to select return page" - {
      "when the user is authenticated" - {
        "but the returnPeriod is missing from userAnswers" in {
          build.commonPrecondition

          setAnswers(emptyUserAnswersForCorrectReturn)

          WsTestClient.withClient { client =>
            val result1 = optJson match {
              case Some(json) => createClientRequestPOST(client, url, json)
              case _          => createClientRequestGet(client, url)
            }

            whenReady(result1) { res =>
              res.status mustBe 303
              res
                .header(HeaderNames.LOCATION)
                .get mustBe controllers.correctReturn.routes.SelectController.onPageLoad.url
            }
          }
        }

        "but there is no sdilReturn for correctReturnPeriod" in {
          build.commonPrecondition

          setUpForCorrectReturn(emptyUserAnswersForCorrectReturn, None)
          WsTestClient.withClient { client =>
            val result1 = optJson match {
              case Some(json) => createClientRequestPOST(client, url, json)
              case _          => createClientRequestGet(client, url)
            }

            whenReady(result1) { res =>
              res.status mustBe 303
              res
                .header(HeaderNames.LOCATION)
                .get mustBe controllers.correctReturn.routes.SelectController.onPageLoad.url
            }
          }
        }
      }
    }

  def testUnauthorisedUser(url: String, optJson: Option[JsValue] = None): Unit = {
    "the user is unauthenticated" - {
      "redirect to gg-signin" in {
        build.unauthorisedPrecondition

        WsTestClient.withClient { client =>
          val result1 = optJson match {
            case Some(json) => createClientRequestPOST(client, url, json)
            case _          => createClientRequestGet(client, url)
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
        build.authorisedButNoEnrolmentsPrecondition

        WsTestClient.withClient { client =>
          val result1 = optJson match {
            case Some(json) => createClientRequestPOST(client, url, json)
            case _          => createClientRequestGet(client, url)
          }

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).get must include("/unauthorised")
          }
        }
      }
    }
    "the user is authenticated but has no sdil subscription" - {
      "redirect to sdil home" in {
        build.authorisedButNoSdilSubscriptionPrecondition

        WsTestClient.withClient { client =>
          val result1 = optJson match {
            case Some(json) => createClientRequestPOST(client, url, json)
            case _          => createClientRequestGet(client, url)
          }

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).get must include("/soft-drinks-industry-levy-account-frontend/home")
          }
        }
      }
    }
  }

  def testAuthenticatedUserButNoUserAnswers(url: String, optJson: Option[JsValue] = None): Unit =
    "the user is authenticated but has no sdil subscription" - {
      "redirect to select change controller" in {
        build.commonPrecondition

        remove(sdilNumber)

        WsTestClient.withClient { client =>
          val result1 = optJson match {
            case Some(json) => createClientRequestPOST(client, url, json)
            case _          => createClientRequestGet(client, url)
          }

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION).get must include("/select-change")
          }
        }
      }
    }

  def testAuthenticatedWithUserAnswersForUnsupportedJourneyType(
    currentJourney: SelectChange,
    url: String,
    optJson: Option[JsValue] = None
  ): Unit = {
    val journeyTypesNotSupported = currentJourney match {
      case SelectChange.CancelRegistration =>
        SelectChange.values.filter(!List(currentJourney, SelectChange.ChangeActivity).contains(_))
      case _ => SelectChange.values.filter(_ != currentJourney)
    }
    "when the user is authenticated with sdilSubscription" - {
      journeyTypesNotSupported.foreach { unsupportedJourney =>
        s"and has user answers for $unsupportedJourney" - {
          "should redirect to select change controller" in {
            build.commonPrecondition

            setAnswers(emptyUserAnswersForSelectChange(unsupportedJourney))

            WsTestClient.withClient { client =>
              val result1 = optJson match {
                case Some(json) => createClientRequestPOST(client, url, json)
                case _          => createClientRequestGet(client, url)
              }

              whenReady(result1) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION).get must include("/select-change")
              }
            }
          }
        }
      }
    }
  }
}
