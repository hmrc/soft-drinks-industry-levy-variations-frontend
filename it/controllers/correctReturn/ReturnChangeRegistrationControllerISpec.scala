package controllers.correctReturn

import controllers.ControllerITTestHelper
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import play.api.i18n.Messages
import play.api.test.WsTestClient
import models.SelectChange.CorrectReturn

class ReturnChangeRegistrationControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/return-change-registration"

  "GET " + normalRoutePath - {
    "should return OK and render the ReturnChangeRegistration page" in {
      given
        .commonPrecondition

      setAnswers(emptyUserAnswersForCorrectReturn)

      WsTestClient.withClient { client =>
        val result1 = createClientRequestGet(client, correctReturnBaseUrl + normalRoutePath)

        whenReady(result1) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title must include(Messages("correctReturn.returnChangeRegistration" + ".title"))
        }
      }
    }
    testUnauthorisedUser(correctReturnBaseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + normalRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + normalRoutePath)
  }
}
