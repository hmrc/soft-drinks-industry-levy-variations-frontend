package controllers.correctReturn

import controllers.ControllerITTestHelper
import models.{CheckMode, NormalMode}
import models.SelectChange.CorrectReturn
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.correctReturn.ClaimCreditsForExportsPage
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient

class ClaimCreditsForExportsControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/claim-credits-for-exports"
  val checkRoutePath = "/change-claim-credits-for-exports"

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the ClaimCreditsForExports page with no data populated" in {
        given
          .commonPrecondition

        setUpForCorrectReturn(emptyUserAnswersForCorrectReturn)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, correctReturnBaseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("correctReturn.claimCreditsForExports" + ".title"))
            val radioInputs = page.getElementsByClass("govuk-radios__input")
            radioInputs.size() mustBe 2
            radioInputs.get(0).attr("value") mustBe "true"
            radioInputs.get(0).hasAttr("checked") mustBe false
            radioInputs.get(1).attr("value") mustBe "false"
            radioInputs.get(1).hasAttr("checked") mustBe false
          }
        }
      }
    }

    userAnswersForCorrectReturnClaimCreditsForExportsPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page with " + key + " radio checked" in {
          given
            .commonPrecondition

          setUpForCorrectReturn(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, correctReturnBaseUrl + normalRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("correctReturn.claimCreditsForExports" + ".title"))
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe 2
              radioInputs.get(0).attr("value") mustBe "true"
              radioInputs.get(0).hasAttr("checked") mustBe key == "yes"
              radioInputs.get(1).attr("value") mustBe "false"
              radioInputs.get(1).hasAttr("checked") mustBe key == "no"
            }
          }
        }
      }
    }
    testRequiredCorrectReturnDataMissing(correctReturnBaseUrl + normalRoutePath)
    testUnauthorisedUser(correctReturnBaseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + normalRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + normalRoutePath)
  }

  s"GET " + checkRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the ClaimCreditsForExports page with no data populated" in {
        given
          .commonPrecondition

        setUpForCorrectReturn(emptyUserAnswersForCorrectReturn)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, correctReturnBaseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("correctReturn.claimCreditsForExports" + ".title"))
            val radioInputs = page.getElementsByClass("govuk-radios__input")
            radioInputs.size() mustBe 2
            radioInputs.get(0).attr("value") mustBe "true"
            radioInputs.get(0).hasAttr("checked") mustBe false
            radioInputs.get(1).attr("value") mustBe "false"
            radioInputs.get(1).hasAttr("checked") mustBe false
          }
        }
      }
    }

    userAnswersForCorrectReturnClaimCreditsForExportsPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page with " + key + " radio checked" in {
          given
            .commonPrecondition

          setUpForCorrectReturn(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, correctReturnBaseUrl + checkRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("correctReturn.claimCreditsForExports" + ".title"))
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe 2
              radioInputs.get(0).attr("value") mustBe "true"
              radioInputs.get(0).hasAttr("checked") mustBe key == "yes"
              radioInputs.get(1).attr("value") mustBe "false"
              radioInputs.get(1).hasAttr("checked") mustBe key == "no"
            }
          }
        }
      }
    }

    testRequiredCorrectReturnDataMissing(correctReturnBaseUrl + checkRoutePath)
    testUnauthorisedUser(correctReturnBaseUrl + checkRoutePath)
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + checkRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + checkRoutePath)
  }

  s"POST " + normalRoutePath - {
    userAnswersForCorrectReturnClaimCreditsForExportsPage.foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        "should update the session with the new value and redirect to the index controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setUpForCorrectReturn(emptyUserAnswersForCorrectReturn)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, correctReturnBaseUrl + normalRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                val expectedLocation = if (yesSelected) {
                  routes.HowManyClaimCreditsForExportsController.onPageLoad(NormalMode).url
                } else {
                  routes.ClaimCreditsForLostDamagedController.onPageLoad(NormalMode).url
                }
                res.header(HeaderNames.LOCATION) mustBe Some(expectedLocation)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(ClaimCreditsForExportsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }

          "when the session already contains data for page" in {
            given
              .commonPrecondition

            setUpForCorrectReturn(userAnswers)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, correctReturnBaseUrl + normalRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                val expectedLocation = if (yesSelected) {
                  routes.HowManyClaimCreditsForExportsController.onPageLoad(NormalMode).url
                } else {
                  routes.ClaimCreditsForLostDamagedController.onPageLoad(NormalMode).url
                }
                res.header(HeaderNames.LOCATION) mustBe Some(expectedLocation)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(ClaimCreditsForExportsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }
        }
      }
    }

    "when the user does not select yes or no" - {
      "should return 400 with required error" in {
        given
          .commonPrecondition

        setUpForCorrectReturn(emptyUserAnswersForCorrectReturn)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, correctReturnBaseUrl + normalRoutePath, Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("correctReturn.claimCreditsForExports" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe Messages("correctReturn.claimCreditsForExports" + ".error.required")
          }
        }
      }
    }
    testUnauthorisedUser(correctReturnBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
  }

  s"POST " + checkRoutePath - {
    userAnswersForCorrectReturnClaimCreditsForExportsPage.foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        val yesSelected = key == "yes"
        "should update the session with the new value and redirect to the checkAnswers controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setUpForCorrectReturn(emptyUserAnswersForCorrectReturn)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, correctReturnBaseUrl + checkRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                val expectedLocation = if(yesSelected) {
                  routes.HowManyClaimCreditsForExportsController.onPageLoad(CheckMode).url
                } else {
                  routes.CorrectReturnCYAController.onPageLoad.url
                }
                res.header(HeaderNames.LOCATION) mustBe Some(expectedLocation)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(ClaimCreditsForExportsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }

          "when the session already contains data for page" in {
            given
              .commonPrecondition

            setUpForCorrectReturn(userAnswers)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, correctReturnBaseUrl + checkRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                val expectedLocation = if (yesSelected) {
                  routes.HowManyClaimCreditsForExportsController.onPageLoad(CheckMode).url
                } else {
                  routes.CorrectReturnCYAController.onPageLoad.url
                }
                res.header(HeaderNames.LOCATION) mustBe Some(expectedLocation)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(ClaimCreditsForExportsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }
        }
      }
    }

    "when the user does not select yes or no" - {
      "should return 400 with required error" in {
        given
          .commonPrecondition

        setUpForCorrectReturn(emptyUserAnswersForCorrectReturn)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, correctReturnBaseUrl + checkRoutePath, Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("correctReturn.claimCreditsForExports" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe Messages("correctReturn.claimCreditsForExports" + ".error.required")
          }
        }
      }
    }
    testUnauthorisedUser(correctReturnBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
  }
}
