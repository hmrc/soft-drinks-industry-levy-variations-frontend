package controllers.cancelRegistration

import controllers.ControllerITTestHelper
import models.SelectChange.CancelRegistration
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.test.WsTestClient

class CancellationRequestDoneControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/cancellation-request-done"

  lazy val cancellationRequestDoneRoute: String = routes.CancellationRequestDoneController.onPageLoad().url

  "GET " + normalRoutePath - {
    "should return OK and render the CancellationRequestDone page" in {
      given
        .commonPrecondition

      setAnswers(emptyUserAnswersForCancelRegistration)

      WsTestClient.withClient { client =>
        val result1 = createClientRequestGet(client, cancelRegistrationBaseUrl + normalRoutePath)

        whenReady(result1) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title mustBe "Cancellation request sent - Soft Drinks Industry Levy - GOV.UK"
        }
      }
    }
    testUnauthorisedUser(cancelRegistrationBaseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(cancelRegistrationBaseUrl + normalRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CancelRegistration, cancelRegistrationBaseUrl + normalRoutePath)

  }
}
