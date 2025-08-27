package controllers.correctReturn

import controllers.LitresISpecHelper
import models.SelectChange.CorrectReturn
import models.{CheckMode, LitresInBands, NormalMode, UserAnswers}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers._
import pages.correctReturn.{ClaimCreditsForLostDamagedPage, HowManyCreditsForLostDamagedPage}
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.WsTestClient

class HowManyCreditsForLostDamagedControllerISpec extends LitresISpecHelper {

  val normalRoutePath = "/how-many-credits-for-lost-damaged"
  val checkRoutePath = "/change-how-many-credits-for-lost-damaged"

  val userAnswers: UserAnswers = emptyUserAnswersForCorrectReturn.set(HowManyCreditsForLostDamagedPage, litresInBands).success.value

  List(NormalMode, CheckMode).foreach { mode =>
    val (path, redirectLocation) = if (mode == NormalMode) {
      (normalRoutePath, routes.CorrectReturnCYAController.onPageLoad.url)
    } else {
      (checkRoutePath, routes.CorrectReturnCYAController.onPageLoad.url)
    }

    "GET " + path - {
      "when the userAnswers contains no data" - {
        "should return OK and render the litres page for CreditsForLostDamaged with no data populated" in {
          build
            .commonPrecondition

          setUpForCorrectReturn(emptyUserAnswersForCorrectReturn)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, correctReturnBaseUrl + path)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title mustBe "How many credits do you want to claim for liable drinks which have been lost or destroyed? - Soft Drinks Industry Levy - GOV.UK"
              testLitresInBandsNoPrepopulatedData(page)
            }
          }
        }
      }

      s"when the userAnswers contains data for the page" - {
        s"should return OK and render the page with fields populated" in {
          build
            .commonPrecondition

          setUpForCorrectReturn(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, correctReturnBaseUrl + path)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title mustBe "How many credits do you want to claim for liable drinks which have been lost or destroyed? - Soft Drinks Industry Levy - GOV.UK"
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
            build
              .commonPrecondition

            setUpForCorrectReturn(emptyUserAnswersForCorrectReturn)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, correctReturnBaseUrl + path, Json.toJson(litresInBandsObj)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(redirectLocation)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyCreditsForLostDamagedPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe litresInBands
              }
            }
          }

          "when the session already contains data for page" in {
            build
              .commonPrecondition

            setUpForCorrectReturn(userAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, correctReturnBaseUrl + path, Json.toJson(litresInBandsDiffObj)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(redirectLocation)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[LitresInBands]](None)(_.get(HowManyCreditsForLostDamagedPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe litresInBandsDiff
              }
            }
          }
        }
      }

      "should return 400 with required error" - {
        val errorTitle =
          "Error: How many credits do you want to claim for liable drinks which have been lost or destroyed? - Soft Drinks Industry Levy - GOV.UK"

        "when no questions are answered" in {
          build
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
          build
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
          build
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
          build
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
          build
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
          build
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


      "should redirect to Change Registration in Returns controller when user is a new packer or importer" in {
        build
          .commonPreconditionChangeSubscription(diffSubscription)


        setUpForCorrectReturn(completedUserAnswersForCorrectReturnNewPackerOrImporter.set(ClaimCreditsForLostDamagedPage, true).success.value)

        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, correctReturnBaseUrl + normalRoutePath, Json.toJson(litresInBandsObj)
          )

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.ReturnChangeRegistrationController.onPageLoad(NormalMode).url)
          }
        }
      }


    }
  }
}
