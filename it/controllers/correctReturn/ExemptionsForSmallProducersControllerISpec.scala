package controllers.correctReturn

import controllers.ControllerITTestHelper
import models.{CheckMode, NormalMode}
import models.SelectChange.CorrectReturn
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.correctReturn.ExemptionsForSmallProducersPage
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient

class ExemptionsForSmallProducersControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/exemptions-for-small-producers"
  val checkRoutePath = "/change-exemptions-for-small-producers"

  List(normalRoutePath, checkRoutePath).foreach { route =>
    "GET " + route - {
      "when the userAnswers contains no data" - {
        "should return OK and render the ExemptionsForSmallProducers page with no data populated" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersForCorrectReturn)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, correctReturnBaseUrl + route)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("correctReturn.exemptionsForSmallProducers" + ".title"))
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

      userAnswersForExceptionsForSmallProducersPage.foreach { case (key, userAnswers) =>
        s"when the userAnswers contains data for the page with " + key + " selected" - {
          s"should return OK and render the page with " + key + " radio checked" in {
            given
              .commonPrecondition

            setAnswers(userAnswers)

            WsTestClient.withClient { client =>
              val result1 = createClientRequestGet(client, correctReturnBaseUrl + route)

              whenReady(result1) { res =>
                res.status mustBe 200
                val page = Jsoup.parse(res.body)
                page.title must include(Messages("correctReturn.exemptionsForSmallProducers" + ".title"))
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
      testUnauthorisedUser(correctReturnBaseUrl + route)
      testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + route)
      testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + route)
    }

    s"POST " + route - {
      userAnswersForExceptionsForSmallProducersPage.foreach { case (key, userAnswers) =>
        "when the user selects " + key - {
          "should update the session with the new value and redirect to the index controller" - {
            "when the session contains no data for page" in {
              given
                .commonPrecondition

              setAnswers(emptyUserAnswersForCorrectReturn)
              WsTestClient.withClient { client =>
                val yesSelected = key == "yes"
                val result = createClientRequestPOST(
                  client, correctReturnBaseUrl + route, Json.obj("value" -> yesSelected.toString)
                )

                whenReady(result) { res =>
                  res.status mustBe 303

                  val expectedLocation = if (key == "yes") {
                    if (route == checkRoutePath) routes.SmallProducerDetailsController.onPageLoad(CheckMode).url else routes.AddASmallProducerController.onPageLoad(NormalMode).url
                  } else if (route == checkRoutePath) {
                    routes.CorrectReturnCYAController.onPageLoad.url
                  } else {
                    routes.BroughtIntoUKController.onPageLoad(NormalMode).url
                  }
                  res.header(HeaderNames.LOCATION) mustBe Some(expectedLocation)
                  val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(ExemptionsForSmallProducersPage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe yesSelected
                }
              }
            }

            "when the session already contains data for page" in {
              given
                .commonPrecondition

              setAnswers(userAnswers)
              WsTestClient.withClient { client =>
                val yesSelected = key == "yes"

                val result = createClientRequestPOST(
                  client, correctReturnBaseUrl + route, Json.obj("value" -> yesSelected.toString)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  val expectedLocation = if (key == "yes") {
                    if (route == checkRoutePath) routes.SmallProducerDetailsController.onPageLoad(CheckMode).url else routes.AddASmallProducerController.onPageLoad(NormalMode).url
                  } else if (route == checkRoutePath) {
                    routes.CorrectReturnCYAController.onPageLoad.url
                  } else {
                    routes.BroughtIntoUKController.onPageLoad(NormalMode).url
                  }
                  res.header(HeaderNames.LOCATION) mustBe Some(expectedLocation)
                  val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(ExemptionsForSmallProducersPage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe yesSelected
                }
              }
            }
          }
        }
      }

      "when the user does not select an option" - {
        "should return 400 with required error" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersForCorrectReturn)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, correctReturnBaseUrl + route, Json.obj("value" -> "")
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.title must include("Error: " + Messages("correctReturn.exemptionsForSmallProducers" + ".title"))
              val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
                .first()
              errorSummary
                .select("a")
                .attr("href") mustBe "#value"
              errorSummary.text() mustBe Messages("correctReturn.exemptionsForSmallProducers" + ".error.required")
            }
          }
        }
      }
      testUnauthorisedUser(correctReturnBaseUrl + route, Some(Json.obj("value" -> "true")))
      testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + route, Some(Json.obj("value" -> "true")))
      testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + route, Some(Json.obj("value" -> "true")))
    }
  }
}
