package controllers.correctReturn

import controllers.LitresISpecHelper
import models.SelectChange.CorrectReturn
import models.{CheckMode, LitresInBands, NormalMode, UserAnswers}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import pages.correctReturn.HowManyOperatePackagingSiteOwnBrandsPage
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.WsTestClient

class HowManyOperatePackagingSiteOwnBrandsControllerISpec extends LitresISpecHelper {

  val normalRoutePath = "/how-many-own-brands-packaged-at-own-sites"
  val checkRoutePath = "/change-how-many-own-brands-packaged-at-own-sites"

  val userAnswers: UserAnswers = emptyUserAnswersForCorrectReturn.set(HowManyOperatePackagingSiteOwnBrandsPage, litresInBands).success.value

  List(NormalMode, CheckMode).foreach { mode =>
    val (path, redirectLocation) = if(mode == NormalMode) {
      (normalRoutePath, routes.PackagedAsContractPackerController.onPageLoad(NormalMode).url)
    } else {
      (checkRoutePath, routes.CorrectReturnCYAController.onPageLoad.url)
    }

    "GET " + path - {
      "when the userAnswers contains no data" - {
        "should return OK and render the litres page for OperatePackagingSiteOwnBrands with no data populated" in {
          given
            .commonPrecondition

          setUpForCorrectReturn(emptyUserAnswersForCorrectReturn)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, correctReturnBaseUrl + path)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title mustBe "How many litres of liable drinks have you packaged at UK sites you operate? - Soft Drinks Industry Levy - GOV.UK"
              testLitresInBandsNoPrepopulatedData(page)
            }
          }
        }
      }

      s"when the userAnswers contains data for the page" - {
        s"should return OK and render the page with fields populated" in {
          given
            .commonPrecondition

          setUpForCorrectReturn(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, correctReturnBaseUrl + path)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title mustBe "How many litres of liable drinks have you packaged at UK sites you operate? - Soft Drinks Industry Levy - GOV.UK"
              testLitresInBandsWithPrepopulatedData(page)
            }
          }
        }
      }
      testRequiredCorrectReturnDataMissing(correctReturnBaseUrl + path)
      testUnauthorisedUser(correctReturnBaseUrl + path)
      testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + path)
      testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + path)
    }

    s"POST " + path - {
      "when the user populates all litres fields" - {
        "should update the session with the new values and redirect to " + redirectLocation - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setUpForCorrectReturn(emptyUserAnswersForCorrectReturn)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, correctReturnBaseUrl + path, Json.toJson(litresInBandsObj)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(redirectLocation)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyOperatePackagingSiteOwnBrandsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe litresInBands
              }
            }
          }

          "when the session already contains data for page" in {
            given
              .commonPrecondition

            setUpForCorrectReturn(userAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, correctReturnBaseUrl + path, Json.toJson(litresInBandsDiffObj)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(redirectLocation)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyOperatePackagingSiteOwnBrandsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe litresInBandsDiff
              }
            }
          }
        }
      }

      "should return 400 with required error" - {
        val errorTitle = "Error: How many litres of liable drinks have you packaged at UK sites you operate? - Soft Drinks Industry Levy - GOV.UK"

        "when no questions are answered" in {
          given
            .commonPrecondition

          setUpForCorrectReturn(emptyUserAnswersForCorrectReturn)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, correctReturnBaseUrl + path, emptyJson
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

          setUpForCorrectReturn(emptyUserAnswersForCorrectReturn)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, correctReturnBaseUrl + path, jsonWithNoNumeric
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

          setUpForCorrectReturn(emptyUserAnswersForCorrectReturn)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, correctReturnBaseUrl + path, jsonWithNegativeNumber
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

          setUpForCorrectReturn(emptyUserAnswersForCorrectReturn)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, correctReturnBaseUrl + path, jsonWithDecimalNumber
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

          setUpForCorrectReturn(emptyUserAnswersForCorrectReturn)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, correctReturnBaseUrl + path, jsonWithOutOfRangeNumber
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              testOutOfMaxValFormErrors(page, errorTitle)
            }
          }
        }

        "when the user answers with 0" in {
          given
            .commonPrecondition

          setUpForCorrectReturn(emptyUserAnswersForCorrectReturn)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, correctReturnBaseUrl + path, jsonWith0
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              testZeroFormErrors(page, errorTitle)
            }
          }
        }
      }

      testUnauthorisedUser(correctReturnBaseUrl + path, Some(Json.toJson(litresInBandsDiff)))
      testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + path, Some(Json.toJson(litresInBandsDiff)))
      testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + path, Some(Json.toJson(litresInBandsDiff)))
    }
  }
}
