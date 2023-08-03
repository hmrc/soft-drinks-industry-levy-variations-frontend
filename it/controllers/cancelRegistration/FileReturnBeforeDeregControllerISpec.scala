package controllers.cancelRegistration

import controllers.{ControllerITTestHelper, routes}
import models.NormalMode
import models.SelectChange.CancelRegistration
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.test.WsTestClient

class FileReturnBeforeDeregControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/file-return-before-deregistration"

  "GET " + normalRoutePath - {
    "should return OK and render the FileReturnBeforeDereg page" in {
      given
        .commonPrecondition

      setAnswers(emptyUserAnswersForCancelRegistration)

      WsTestClient.withClient { client =>
        val result1 = createClientRequestGet(client, cancelRegistrationBaseUrl + normalRoutePath)

        whenReady(result1) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title must include(Messages("You cannot cancel your registration while you have returns to send - Soft Drinks Industry Levy - GOV.UK"))
        }
      }
    }

    "should redirect when no returns are pending" in {
      given.commonPreconditionEmptyReturn

      setAnswers(emptyUserAnswersForCancelRegistration)

      WsTestClient.withClient { client =>
        val result = createClientRequestGet(client, cancelRegistrationBaseUrl + normalRoutePath)

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.cancelRegistration.routes.ReasonController.onPageLoad(NormalMode).url)
        }
      }
    }

    "should redirect when check returns returns Not Found" in {
      given.returnPendingNotFoundPreCondition

      setAnswers(emptyUserAnswersForCancelRegistration)

      WsTestClient.withClient { client =>
        val result = createClientRequestGet(client, cancelRegistrationBaseUrl + normalRoutePath)

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(routes.JourneyRecoveryController.onPageLoad().url)
        }
      }
    }

    testUnauthorisedUser(cancelRegistrationBaseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(cancelRegistrationBaseUrl + normalRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CancelRegistration, cancelRegistrationBaseUrl + normalRoutePath)
  }
}
