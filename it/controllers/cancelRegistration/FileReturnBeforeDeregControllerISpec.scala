package controllers.cancelRegistration

import controllers.ControllerITTestHelper
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
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

    "should redirect when no returns pending are found" in {
      given
        .commonPrecondition.sdilBackend.no_returns_pending("0000001611")

      setAnswers(emptyUserAnswersForCancelRegistration)

      WsTestClient.withClient { client =>
        val result1 = createClientRequestGet(client, cancelRegistrationBaseUrl + normalRoutePath)

        whenReady(result1) { res =>
          res.status mustBe 303
        }
      }
    }

    testUnauthorisedUser(cancelRegistrationBaseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(cancelRegistrationBaseUrl + normalRoutePath)
  }
}
