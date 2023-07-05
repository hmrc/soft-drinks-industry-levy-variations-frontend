package controllers.changeActivity

import controllers.ControllerITTestHelper
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import play.api.http.Status.OK
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames

class ChangeActivityCYAControllerISpec extends ControllerITTestHelper {

  val route = "/change-activity/check-your-answers"
  "GET " + routes.ChangeActivityCYAController.onPageLoad.url - {
    "when the userAnswers contains no data" - {
      "should render the page" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, baseUrl + route)

          whenReady(result) { res =>
            res.status mustBe OK
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("changeActivity.checkYourAnswers.title"))
            page.getElementsByClass("govuk-summary-list").size() mustBe 4
          }
        }
      }
    }
//  TODO: Add tests for whether data that is present is seen
    testUnauthorisedUser(baseUrl + route)
    testAuthenticatedUserButNoUserAnswers(baseUrl + route)
  }
  "POST " + routes.ChangeActivityCYAController.onSubmit.url - {
    "when the userAnswers contains no data" - {
      "should redirect to next page" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)

        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(client, baseUrl + route, Json.obj())

          whenReady(result) { res =>
            res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.IndexController.onPageLoad.url)
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + route, Some(Json.obj()))
    testAuthenticatedUserButNoUserAnswers(baseUrl + route, Some(Json.obj()))
  }
}