package controllers.cancelRegistration

import controllers.ControllerITTestHelper
import models.SelectChange.CancelRegistration
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.*
import play.api.i18n.{ Messages, MessagesApi }
import play.api.test.{ FakeRequest, WsTestClient }

class FileReturnBeforeDeregControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/file-return-before-deregistration"

  given messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  given messages: Messages = messagesApi.preferred(FakeRequest())

  "GET " + normalRoutePath - {
    "should return OK and render the FileReturnBeforeDereg page" in {
      build.commonPrecondition

      setAnswers(emptyUserAnswersForCancelRegistration)

      WsTestClient.withClient { client =>
        val result1 = createClientRequestGet(client, cancelRegistrationBaseUrl + normalRoutePath)

        whenReady(result1) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title must include(
            Messages(
              "You cannot cancel your registration while you have returns to send - Soft Drinks Industry Levy - GOV.UK"
            )
          )
        }
      }
    }

    "should redirect when no returns pending are found" in {
      build.commonPrecondition.sdilBackend.no_returns_pending("0000001611")

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
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(
      CancelRegistration,
      cancelRegistrationBaseUrl + normalRoutePath
    )
  }
}
