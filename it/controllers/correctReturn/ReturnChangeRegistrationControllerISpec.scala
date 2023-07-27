package controllers.correctReturn

import controllers.ControllerITTestHelper
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import play.api.i18n.Messages
import play.api.test.WsTestClient
import models.SelectChange.CorrectReturn
import models.UserAnswers
import pages.correctReturn.ReturnChangeRegistrationPage
import play.api.libs.json.Json
import play.mvc.Http.HeaderNames

class ReturnChangeRegistrationControllerISpec extends ControllerITTestHelper {

  val returnDiff = "testing456"
  val userAnswers: UserAnswers = emptyUserAnswersForCorrectReturn.set(ReturnChangeRegistrationPage, returnDiff).success.value
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
          page.title must include(Messages("You changed your soft drinks business activity - Soft Drinks Industry Levy - GOV.UK"))
        }
      }
    }
    testUnauthorisedUser(correctReturnBaseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + normalRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + normalRoutePath)
  }

  s"POST " + normalRoutePath - {
    "when the user answers the question" - {
      "should update the session with the new values and redirect to the index controller" - {
        "when the session contains no data for page" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersForCorrectReturn)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, correctReturnBaseUrl + normalRoutePath, Json.obj("value" -> returnDiff)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(defaultCall.url)
            }
          }
        }
      }
    }

    testUnauthorisedUser(correctReturnBaseUrl + normalRoutePath, Some(Json.obj("value" -> returnDiff)))
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + normalRoutePath, Some(Json.obj("value" -> returnDiff)))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + normalRoutePath,
      Some(Json.obj("value" -> returnDiff)))
  }
}
