package controllers.changeActivity

import controllers.LitresISpecHelper
import models.SelectChange.ChangeActivity
import models.{ CheckMode, LitresInBands, NormalMode, UserAnswers }
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers._
import pages.changeActivity.HowManyOperatePackagingSiteOwnBrandsPage
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.WsTestClient

class HowManyOperatePackagingSiteOwnBrandsControllerISpec extends LitresISpecHelper {

  val normalRoutePath = "/how-many-own-brands-next-12-months"
  val checkRoutePath = "/change-how-many-own-brands-next-12-months"

  val userAnswers: UserAnswers =
    emptyUserAnswersForChangeActivity.set(HowManyOperatePackagingSiteOwnBrandsPage, litresInBands).success.value

  List(NormalMode, CheckMode).foreach { mode =>
    val (path, redirectLocation) = if (mode == NormalMode) {
      (normalRoutePath, routes.ContractPackingController.onPageLoad(NormalMode).url)
    } else {
      (checkRoutePath, routes.ChangeActivityCYAController.onPageLoad().url)
    }

    "GET " + path - {
      "when the userAnswers contains no data" - {
        "should return OK and render the litres page for OperatePackagingSiteOwnBrands with no data populated" in {
          build.commonPrecondition

          setAnswers(emptyUserAnswersForChangeActivity)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, changeActivityBaseUrl + path)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title mustBe "How many litres of your own brands will you package in the next 12 months? - Soft Drinks Industry Levy - GOV.UK"
              testLitresInBandsNoPrepopulatedData(page)
            }
          }
        }
      }

      s"when the userAnswers contains data for the page" - {
        s"should return OK and render the page with fields populated" in {
          build.commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, changeActivityBaseUrl + path)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title mustBe "How many litres of your own brands will you package in the next 12 months? - Soft Drinks Industry Levy - GOV.UK"
              testLitresInBandsWithPrepopulatedData(page)
            }
          }
        }
      }
      testUnauthorisedUser(changeActivityBaseUrl + path)
      testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + path)
      testAuthenticatedWithUserAnswersForUnsupportedJourneyType(ChangeActivity, changeActivityBaseUrl + path)
    }

    s"POST " + path - {
      "when the user populates all litres fields" - {
        "should update the session with the new values and redirect to " + redirectLocation - {
          "when the session contains no data for page" in {
            build.commonPrecondition

            setAnswers(emptyUserAnswersForChangeActivity)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client,
                changeActivityBaseUrl + path,
                Json.toJson(litresInBandsObj)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(redirectLocation)
                val dataStoredForPage = getAnswers(userAnswers.id)
                  .fold[Option[LitresInBands]](None)(_.get(HowManyOperatePackagingSiteOwnBrandsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe litresInBands
              }
            }
          }

          "when the session already contains data for page" in {
            build.commonPrecondition

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client,
                changeActivityBaseUrl + path,
                Json.toJson(litresInBandsDiffObj)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(redirectLocation)
                val dataStoredForPage = getAnswers(userAnswers.id)
                  .fold[Option[LitresInBands]](None)(_.get(HowManyOperatePackagingSiteOwnBrandsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe litresInBandsDiff
              }
            }
          }
        }
      }

      "should return 400 with required error" - {
        val errorTitle =
          "Error: How many litres of your own brands will you package in the next 12 months? - Soft Drinks Industry Levy - GOV.UK"

        "when no questions are answered" in {
          build.commonPrecondition

          setAnswers(emptyUserAnswersForChangeActivity)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client,
              changeActivityBaseUrl + path,
              emptyJson
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              testEmptyFormErrors(page, errorTitle)
            }
          }
        }

        "when the user answers with no numeric answers" in {
          build.commonPrecondition

          setAnswers(emptyUserAnswersForChangeActivity)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client,
              changeActivityBaseUrl + path,
              jsonWithNoNumeric
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              testNoNumericFormErrors(page, errorTitle)
            }
          }
        }

        "when the user answers with negative numbers" in {
          build.commonPrecondition

          setAnswers(emptyUserAnswersForChangeActivity)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client,
              changeActivityBaseUrl + path,
              jsonWithNegativeNumber
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              testNegativeFormErrors(page, errorTitle)
            }
          }
        }

        "when the user answers with decimal numbers" in {
          build.commonPrecondition

          setAnswers(emptyUserAnswersForChangeActivity)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client,
              changeActivityBaseUrl + path,
              jsonWithDecimalNumber
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              testDecimalFormErrors(page, errorTitle)
            }
          }
        }

        "when the user answers with out of max range numbers" in {
          build.commonPrecondition

          setAnswers(emptyUserAnswersForChangeActivity)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client,
              changeActivityBaseUrl + path,
              jsonWithOutOfRangeNumber
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              testOutOfMaxValFormErrors(page, errorTitle)
            }
          }
        }

        "when the user answers with 0" in {
          build.commonPrecondition

          setAnswers(emptyUserAnswersForChangeActivity)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client,
              changeActivityBaseUrl + path,
              jsonWith0
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              testZeroFormErrors(page, errorTitle)
            }
          }
        }
      }

      testUnauthorisedUser(changeActivityBaseUrl + path, Some(Json.toJson(litresInBandsDiff)))
      testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + path, Some(Json.toJson(litresInBandsDiff)))
      testAuthenticatedWithUserAnswersForUnsupportedJourneyType(
        ChangeActivity,
        changeActivityBaseUrl + path,
        Some(Json.toJson(litresInBandsDiff))
      )
    }
  }
}
