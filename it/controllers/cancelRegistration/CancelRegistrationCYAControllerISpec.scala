package controllers.cancelRegistration

import controllers.ControllerITTestHelper
import models.SelectChange.CancelRegistration
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import pages.cancelRegistration.{CancelRegistrationDatePage, ReasonPage}
import play.api.http.HeaderNames
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.test.WsTestClient

import java.time.LocalDate


class CancelRegistrationCYAControllerISpec extends ControllerITTestHelper {

  val route = "/cancel-registration/check-your-answers"

  "GET " + routes.CancelRegistrationCYAController.onPageLoad.url - {
    "when the userAnswers contains no data" - {
      "should redirect to Select Change controller" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForSelectChange(CancelRegistration))

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, baseUrl + route)

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.SelectChangeController.onPageLoad.url)
          }
        }
      }
    }

    "when the user has populated all pages" - {
      "should render the check your answers page with the required details" in {
        val validCancellationDate = LocalDate.now()
        val userAnswers = emptyUserAnswersForCancelRegistration
          .set(ReasonPage, "No longer sell drinks").success.value
          .set(CancelRegistrationDatePage, validCancellationDate).success.value
        given
          .commonPrecondition

        setAnswers(userAnswers)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, baseUrl + route)

          whenReady(result) { res =>
            res.status mustBe OK
            val page = Jsoup.parse(res.body)
            page.title mustBe "Cancel your Soft Drinks Industry Levy registration - Soft Drinks Industry Levy - GOV.UK"
            page.getElementsByClass("govuk-summary-list__row").size() mustBe 2

            val reasonRow = page.getElementsByClass("govuk-summary-list__row").get(0).getElementsByClass("govuk-summary-list__key")
            val dateRow = page.getElementsByClass("govuk-summary-list__row").get(1).getElementsByClass("govuk-summary-list__key")

            reasonRow.get(0).text() mustBe "Reason for cancelling"
            dateRow.get(0).text() mustBe "Date of cancellation"

            page.getElementsByTag("form").first().attr("action") mustBe routes.CancelRegistrationCYAController.onSubmit.url
            page.getElementsByTag("form").first().getElementsByTag("button").first().text() mustBe "Confirm cancellation"
          }
        }
      }
    }

    testUnauthorisedUser(baseUrl + route)
    testAuthenticatedUserButNoUserAnswers(baseUrl + route)
  }

  "POST " + routes.CancelRegistrationCYAController.onSubmit.url - {
    "should redirect to Index controller when user answers empty" in {
      given
        .commonPrecondition

      setAnswers(emptyUserAnswersForSelectChange(CancelRegistration))

      WsTestClient.withClient { client =>
        val result = createClientRequestPOST(client, baseUrl + route, Json.obj())

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.IndexController.onPageLoad.url)
        }
      }
    }

    testUnauthorisedUser(baseUrl + route, optJson = Some(Json.obj()))
    testAuthenticatedUserButNoUserAnswers(baseUrl + route, optJson = Some(Json.obj()))
  }

}
