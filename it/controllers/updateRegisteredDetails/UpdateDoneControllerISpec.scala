package controllers.updateRegisteredDetails

import controllers.ControllerITTestHelper
import models.SelectChange.UpdateRegisteredDetails
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.test.WsTestClient

class UpdateDoneControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/update-done"

  lazy val updateDoneRoute: String = routes.UpdateDoneController.onPageLoad().url

  "GET " + normalRoutePath - {
    "should return OK and render the UpdateDone page" in {
      given
        .commonPrecondition

      setAnswers(emptyUserAnswersForUpdateRegisteredDetails)

      WsTestClient.withClient { client =>
        val result1 = createClientRequestGet(client, updateRegisteredDetailsBaseUrl + normalRoutePath)

        whenReady(result1) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title mustBe "Update sent - Soft Drinks Industry Levy - GOV.UK"
        }
      }
    }
    testUnauthorisedUser(updateRegisteredDetailsBaseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(updateRegisteredDetailsBaseUrl + normalRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(UpdateRegisteredDetails, updateRegisteredDetailsBaseUrl + normalRoutePath)

  }
}
