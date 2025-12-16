package controllers.updateRegisteredDetails

import controllers.ControllerITTestHelper
import models.SelectChange.UpdateRegisteredDetails
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers._
import play.api.test.WsTestClient

import java.time.Instant

class UpdateDoneControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/update-done"

  lazy val updateDoneRoute: String = routes.UpdateDoneController.onPageLoad().url

  "GET " + normalRoutePath - {
    "should return OK and render the UpdateDone page" in {
      build.commonPrecondition

      val testTime = Instant.now()
      setAnswers(emptyUserAnswersForUpdateRegisteredDetails.copy(submitted = true, submittedOn = Some(testTime)))

      WsTestClient.withClient { client =>
        val result1 = createClientRequestGet(client, updateRegisteredDetailsBaseUrl + normalRoutePath)

        whenReady(result1) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title mustBe "Update sent - Soft Drinks Industry Levy - GOV.UK"
        }
      }
    }
    "should redirect when no submitted on time is present" in {
      build.commonPrecondition
      setAnswers(emptyUserAnswersForUpdateRegisteredDetails)

      WsTestClient.withClient { client =>
        val result1 = createClientRequestGet(client, updateRegisteredDetailsBaseUrl + normalRoutePath)

        whenReady(result1) { res =>
          res.status mustBe 303
        }
      }
    }
    testUnauthorisedUser(updateRegisteredDetailsBaseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(updateRegisteredDetailsBaseUrl + normalRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(
      UpdateRegisteredDetails,
      updateRegisteredDetailsBaseUrl + normalRoutePath
    )

  }
}
