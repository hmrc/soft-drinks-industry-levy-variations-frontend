package controllers.cancelRegistration

import controllers.ControllerITTestHelper
import models.SelectChange.CancelRegistration
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers._
import pages.cancelRegistration.{ CancelRegistrationDatePage, ReasonPage }
import play.api.http.Status.SEE_OTHER
import play.api.test.WsTestClient

import java.time.{ Instant, LocalDate }

class CancellationRequestDoneControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/cancellation-request-done"

  lazy val cancellationRequestDoneRoute: String = routes.CancellationRequestDoneController.onPageLoad().url

  "GET " + normalRoutePath - {
    "should return OK and render the CancellationRequestDone page" in {
      build.commonPrecondition
      val testTime = Instant.now()
      val userAnswers = emptyUserAnswersForCancelRegistration
        .set(ReasonPage, "No longer sell drinks")
        .success
        .value
        .set(CancelRegistrationDatePage, LocalDate.now())
        .success
        .value
      setAnswers(userAnswers.copy(submitted = true, submittedOn = Some(testTime)))

      WsTestClient.withClient { client =>
        val result1 = createClientRequestGet(client, cancelRegistrationBaseUrl + normalRoutePath)

        whenReady(result1) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title mustBe "Cancellation request sent - Soft Drinks Industry Levy - GOV.UK"
        }
      }
    }

    "must redirect if there is no submission date" in {
      build.commonPrecondition
      val userAnswers = emptyUserAnswersForCancelRegistration
        .set(ReasonPage, "No longer sell drinks")
        .success
        .value
        .set(CancelRegistrationDatePage, LocalDate.now())
        .success
        .value
      setAnswers(userAnswers)

      WsTestClient.withClient { client =>
        val result1 = createClientRequestGet(client, cancelRegistrationBaseUrl + normalRoutePath)

        whenReady(result1) { res =>
          res.status mustBe SEE_OTHER
        }
      }
    }

    testUnauthorisedUser(cancelRegistrationBaseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(cancelRegistrationBaseUrl + normalRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(
      CancelRegistration,
      cancelRegistrationBaseUrl + normalRoutePath
    )

  }
}
