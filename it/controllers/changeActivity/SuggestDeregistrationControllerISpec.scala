package controllers.changeActivity

import controllers.ControllerITTestHelper
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.test.WsTestClient

class SuggestDeregistrationControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/suggest-deregistration"

  "GET " + normalRoutePath - {
    "should return OK and render the SuggestDeregistration page" in {
      given
        .commonPrecondition

      setAnswers(emptyUserAnswersForChangeActivity)

      WsTestClient.withClient { client =>
        val result1 = createClientRequestGet(client, changeActivityBaseUrl + normalRoutePath)

        whenReady(result1) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title mustBe "You need to cancel your Soft Drinks Industry Levy registration - Soft Drinks Industry Levy - GOV.UK"
        }
      }
    }
    testUnauthorisedUser(changeActivityBaseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + normalRoutePath)
  }

}
