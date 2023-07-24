package controllers.$packageName$

import controllers.LitresISpecHelper
import models.{CheckMode, LitresInBands, NormalMode}
import models.SelectChange.$packageName;format="cap"$
import org.jsoup.Jsoup
import pages.$packageName$.HowMany$className$Page
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}

class HowMany$className$ControllerISpec extends LitresISpecHelper {

  val normalRoutePath = "/$litresUrl$"
  val checkRoutePath = "/change-$litresUrl$"

  val userAnswers = emptyUserAnswersFor$packageName;format="cap"$.set(HowMany$className$Page, litresInBands).success.value

  List(NormalMode, CheckMode).foreach { mode =>
    val (path, redirectLocation) = if(mode == NormalMode) {
      (normalRoutePath, $nextPage$.url)
    } else {
      (checkRoutePath, routes.$packageName;format="cap"$CYAController.onPageLoad.url)
    }

    "GET " + path - {
      "when the userAnswers contains no data" - {
        "should return OK and render the litres page for $className$ with no data populated" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersFor$packageName;format="cap"$)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, $packageName$BaseUrl + path)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("$packageName$.howMany$className$" + ".title"))
              testLitresInBandsNoPrepopulatedData(page)
            }
          }
        }
      }

      s"when the userAnswers contains data for the page" - {
        s"should return OK and render the page with fields populated" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, $packageName$BaseUrl + path)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("$packageName$.howMany$className$" + ".title"))
              testLitresInBandsWithPrepopulatedData(page)
            }
          }
        }
      }
      testUnauthorisedUser($packageName$BaseUrl + path)
      testAuthenticatedUserButNoUserAnswers($packageName$BaseUrl + path)
      testAuthenticatedWithUserAnswersForUnsupportedJourneyType($packageName;format="cap"$, $packageName$BaseUrl + path)
    }

    s"POST " + path - {
      "when the user populates all litres fields" - {
        "should update the session with the new values and redirect to " + redirectLocation - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswersFor$packageName;format="cap"$)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, $packageName$BaseUrl + path, Json.toJson(litresInBands)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(redirectLocation)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowMany$className$Page))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe litresInBands
              }
            }
          }

          "when the session already contains data for page" in {
            given
              .commonPrecondition

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, $packageName$BaseUrl + path, Json.toJson(litresInBandsDiff)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(redirectLocation)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowMany$className$Page))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe litresInBandsDiff
              }
            }
          }
        }
      }

      "should return 400 with required error" - {
        val errorTitle = "Error: " + Messages("$packageName$.howMany$className$.title")

        "when no questions are answered" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersFor$packageName;format="cap"$)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, $packageName$BaseUrl + path, emptyJson
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              testEmptyFormErrors(page, errorTitle)
            }
          }
        }

        "when the user answers with no numeric answers" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersFor$packageName;format="cap"$)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, $packageName$BaseUrl + path, jsonWithNoNumeric
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              testNoNumericFormErrors(page, errorTitle)
            }
          }
        }

        "when the user answers with negative numbers" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersFor$packageName;format="cap"$)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, $packageName$BaseUrl + path, jsonWithNegativeNumber
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              testNegativeFormErrors(page, errorTitle)
            }
          }
        }

        "when the user answers with decimal numbers" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersFor$packageName;format="cap"$)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, $packageName$BaseUrl + path, jsonWithDecimalNumber
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              testDecimalFormErrors(page, errorTitle)
            }
          }
        }

        "when the user answers with out of max range numbers" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersFor$packageName;format="cap"$)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, $packageName$BaseUrl + path, jsonWithOutOfRangeNumber
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              testOutOfMaxValFormErrors(page, errorTitle)
            }
          }
        }
      }

      testUnauthorisedUser($packageName$BaseUrl + path, Some(Json.toJson(litresInBandsDiff)))
      testAuthenticatedUserButNoUserAnswers($packageName$BaseUrl + path, Some(Json.toJson(litresInBandsDiff)))
      testAuthenticatedWithUserAnswersForUnsupportedJourneyType($packageName;format="cap"$, $packageName$BaseUrl + path, Some(Json.toJson(litresInBandsDiff)))
    }
  }
}
