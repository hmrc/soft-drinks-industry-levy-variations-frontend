package controllers

import models.{NormalMode, SelectChange}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers._
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.WsTestClient

class SelectChangeControllerISpec extends ControllerITTestHelper {

  val route = "/select-change"

  s"GET $route" - {
    "should render the select change page" - {
      "that includes the option to correct a return" - {
        "when the user has variable returns" in {
          build
            .commonPrecondition
            .sdilBackend.returns_variable("0000001611")

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + route)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title mustBe "What do you need to do? - Soft Drinks Industry Levy - GOV.UK"
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe 4
              SelectChange.values.zipWithIndex.foreach { case (selectChange, index) =>
                radioInputs.get(index).attr("value") mustBe selectChange.toString
              }
            }
          }
        }
      }

      "that does not include the option to correct a return" - {
        "when the user has no variable returns" in {
          build
            .commonPrecondition
            .sdilBackend.no_returns_variable("0000001611")

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, baseUrl + route)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title mustBe "What do you need to do? - Soft Drinks Industry Levy - GOV.UK"
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe 3
              SelectChange.valuesWithOutCorrectReturns.zipWithIndex.foreach { case (selectChange, index) =>
                radioInputs.get(index).attr("value") mustBe selectChange.toString
              }
            }
          }
        }
      }
    }

    "should render the error page" - {
      "when the call to get variable returns fails" in {
        build
          .commonPrecondition
          .sdilBackend.returns_variable_error("0000001611")

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + route)

          whenReady(result1) { res =>
            res.status mustBe 500
            val page = Jsoup.parse(res.body)
            page.title() mustBe "Sorry, there is a problem with the service - Soft Drinks Industry Levy - GOV.UK"
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + route)
  }

  s"POST $route" - {
    "should generate and save the expected user answers" - {
      "then redirect to the index controller" - {
        "when the user selects to update the registered details" in {
          build
            .commonPrecondition

          WsTestClient.withClient { client =>
            val result1 = createClientRequestPOST(client, baseUrl + route, Json.obj(("value", SelectChange.UpdateRegisteredDetails.toString)))

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some("/soft-drinks-industry-levy-variations-frontend/change-registered-details")
              val expectedUserAnswers = emptyUserAnswersForUpdateRegisteredDetails
              val generatedUserAnswers = getAnswers(sdilNumber)
              generatedUserAnswers mustBe defined
              generatedUserAnswers.get.copy(lastUpdated = expectedUserAnswers.lastUpdated) mustEqual expectedUserAnswers
            }
          }
        }
      }

      "then redirect to correct return select" - {
        "when the user selects to correct return select" in {
          build
            .commonPrecondition

          WsTestClient.withClient { client =>
            val result1 = createClientRequestPOST(client, baseUrl + route, Json.obj(("value", SelectChange.CorrectReturn.toString)))

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(correctReturn.routes.SelectController.onPageLoad.url)
              val expectedUserAnswers = emptyUserAnswersForCorrectReturn.copy(correctReturnPeriod = None)
              val generatedUserAnswers = getAnswers(sdilNumber)
              generatedUserAnswers mustBe defined
              generatedUserAnswers.get.copy(lastUpdated = expectedUserAnswers.lastUpdated) mustEqual expectedUserAnswers
            }
          }
        }
      }

      "then redirect to cancel registration reason" - {
        "when the user selects to cancel registration and has no returns pending" in {
          build
            .commonPrecondition
            .sdilBackend.no_returns_pending("0000001611")

          WsTestClient.withClient { client =>
            val result1 = createClientRequestPOST(client, baseUrl + route, Json.obj(("value", SelectChange.CancelRegistration.toString)))

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(cancelRegistration.routes.ReasonController.onPageLoad(NormalMode).url)
              val expectedUserAnswers = emptyUserAnswersForCancelRegistration
              val generatedUserAnswers = getAnswers(sdilNumber)
              generatedUserAnswers mustBe defined
              generatedUserAnswers.get.copy(lastUpdated = expectedUserAnswers.lastUpdated) mustEqual expectedUserAnswers
            }
          }
        }
      }

      "then redirect to cancel registration file returns before deregistering" - {
        "when the user selects to cancel registration and has returns pending" in {
          build
            .commonPrecondition
            .sdilBackend.returns_pending("0000001611")

          WsTestClient.withClient { client =>
            val result1 = createClientRequestPOST(client, baseUrl + route, Json.obj(("value", SelectChange.CancelRegistration.toString)))

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(cancelRegistration.routes.FileReturnBeforeDeregController.onPageLoad().url)
              val expectedUserAnswers = emptyUserAnswersForCancelRegistration
              val generatedUserAnswers = getAnswers(sdilNumber)
              generatedUserAnswers mustBe defined
              generatedUserAnswers.get.copy(lastUpdated = expectedUserAnswers.lastUpdated) mustEqual expectedUserAnswers
            }
          }
        }
      }

      "then redirect to change activity producer type" - {
        "when the user selects to change activity" in {
          build
            .commonPrecondition

          WsTestClient.withClient { client =>
            val result1 = createClientRequestPOST(client, baseUrl + route, Json.obj(("value", SelectChange.ChangeActivity.toString)))

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(changeActivity.routes.AmountProducedController.onPageLoad(NormalMode).url)
              val expectedUserAnswers = emptyUserAnswersForChangeActivity
              val generatedUserAnswers = getAnswers(sdilNumber)
              generatedUserAnswers mustBe defined
              generatedUserAnswers.get.copy(lastUpdated = expectedUserAnswers.lastUpdated) mustEqual expectedUserAnswers
            }
          }
        }
      }
    }

    "when the user does not select an option" - {
      "should return 400 with required error" in {
        build
          .commonPrecondition
          .sdilBackend.no_returns_variable("0000001611")

        WsTestClient.withClient { client =>
          val result1 = createClientRequestPOST(client, baseUrl + route, Json.obj(("value", "")))

          whenReady(result1) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: What do you need to do? - Soft Drinks Industry Levy - GOV.UK")
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value_0"
            errorSummary.text() mustBe "Select what you need to do"
            getAnswers(emptyUserAnswersForChangeActivity.id).isEmpty mustBe true
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + route, Some(Json.obj("value" -> SelectChange.ChangeActivity.toString)))
  }

}
