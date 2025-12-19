package controllers.changeActivity

import controllers.ControllerITTestHelper
import models.SelectChange.ChangeActivity
import models.changeActivity.AmountProduced
import models.{ NormalMode, UserAnswers }
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers._
import pages.changeActivity.{ AmountProducedPage, ContractPackingPage, ImportsPage }
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.WsTestClient

class SuggestDeregistrationControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/suggest-deregistration"
  val completedUserAnswers: UserAnswers = emptyUserAnswersForChangeActivity
    .set(AmountProducedPage, AmountProduced.None)
    .success
    .value
    .set(ContractPackingPage, false)
    .success
    .value
    .set(ImportsPage, false)
    .success
    .value

  "GET " + normalRoutePath - {
    "should return OK and render the SuggestDeregistration page" in {
      build.commonPrecondition

      setAnswers(completedUserAnswers)

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
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(ChangeActivity, changeActivityBaseUrl + normalRoutePath)
  }

  s"POST " + normalRoutePath - {
    "should redirect to Cancel Registration - File return before deregistration" in {
      build.commonPrecondition

      setAnswers(completedUserAnswers)
      WsTestClient.withClient { client =>
        val result = createClientRequestPOST(
          client,
          changeActivityBaseUrl + normalRoutePath,
          Json.obj()
        )
        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(
            controllers.cancelRegistration.routes.FileReturnBeforeDeregController.onPageLoad().url
          )
        }
      }
    }

    "should redirect to Cancel Registration - Reason Controller, when no returns are pending" in {
      build.returnPendingNotFoundPreCondition

      setAnswers(completedUserAnswers)
      WsTestClient.withClient { client =>
        val result = createClientRequestPOST(
          client,
          changeActivityBaseUrl + normalRoutePath,
          Json.obj()
        )
        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(
            controllers.cancelRegistration.routes.ReasonController.onPageLoad(NormalMode).url
          )
        }
      }
    }

    "should return an internal server error if it fails while trying to get missing returns list" in {
      build.commonPrecondition.sdilBackend.returns_pending_error("0000001611")

      setAnswers(completedUserAnswers)

      WsTestClient.withClient { client =>
        val result = createClientRequestPOST(
          client,
          changeActivityBaseUrl + normalRoutePath,
          Json.obj()
        )
        whenReady(result) { res =>
          res.status mustBe 500
          val page = Jsoup.parse(res.body)
          page.title() mustBe "Sorry, there is a problem with the service - Soft Drinks Industry Levy - GOV.UK"
        }
      }
    }
  }

}
