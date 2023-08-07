package controllers.updateRegisteredDetails

import controllers.ControllerITTestHelper
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import play.api.i18n.Messages
import play.api.test.WsTestClient
import models.SelectChange.UpdateRegisteredDetails

class BusinessAddressControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/business-address"

  "GET " + normalRoutePath - {
    "should return OK and render the BusinessAddress page" in {
      given
        .commonPrecondition

      setAnswers(emptyUserAnswersForUpdateRegisteredDetails)

      WsTestClient.withClient { client =>
        val result1 = createClientRequestGet(client, updateRegisteredDetailsBaseUrl + normalRoutePath)

        whenReady(result1) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title  mustBe "Your business address for the Soft Drinks Industry Levy - Soft Drinks Industry Levy - GOV.UK"
        }
      }
    }

    testUnauthorisedUser(updateRegisteredDetailsBaseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(updateRegisteredDetailsBaseUrl + normalRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(UpdateRegisteredDetails, updateRegisteredDetailsBaseUrl + normalRoutePath)
  }
}
