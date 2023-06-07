package controllers.changeActivity

import controllers.ControllerITTestHelper
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import play.api.i18n.Messages
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
          page.title must include(Messages("changeActivity.suggestDeregistration" + ".title"))
        }
      }
    }
    testUnauthorisedUser(changeActivityBaseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + normalRoutePath)
  }
}
