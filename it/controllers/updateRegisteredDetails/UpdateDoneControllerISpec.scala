package controllers.updateRegisteredDetails

import controllers.ControllerITTestHelper
import models.SelectChange.UpdateRegisteredDetails
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.test.WsTestClient
import java.time.{Instant, LocalDate}

class UpdateDoneControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/update-done"

  lazy val updateDoneRoute: String = routes.UpdateDoneController.onPageLoad().url

  "GET " + normalRoutePath - {
    "should return OK and render the UpdateDone page" in {
      given
        .commonPrecondition

      val testTime = Instant.now()
      setAnswers(emptyUserAnswersForUpdateRegisteredDetails.copy(submittedOn = Some(testTime)))

      WsTestClient.withClient { client =>
        val result1 = createClientRequestGet(client, updateRegisteredDetailsBaseUrl + normalRoutePath)

        whenReady(result1) { res =>
          res.status mustBe 303
        }
      }
    }
    testUnauthorisedUser(updateRegisteredDetailsBaseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(updateRegisteredDetailsBaseUrl + normalRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(UpdateRegisteredDetails, updateRegisteredDetailsBaseUrl + normalRoutePath)

  }
}
