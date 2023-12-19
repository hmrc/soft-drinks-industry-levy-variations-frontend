package controllers.correctReturn

import controllers.ControllerITTestHelper
import models.SelectChange.CorrectReturn
import models.{NormalMode, UserAnswers}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.correctReturn.PackagedAsContractPackerPage
import play.api.i18n.Messages
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames

class ReturnChangeRegistrationControllerISpec extends ControllerITTestHelper {

  val userAnswersNewPacker: UserAnswers = completedUserAnswersForCorrectReturnNewPackerOrImporter
  val userAnswersNewImporterOnly: UserAnswers = completedUserAnswersForCorrectReturnNewPackerOrImporter.set(PackagedAsContractPackerPage, false).success.value

  val normalRoutePath = "/return-change-registration"

  "GET " + normalRoutePath - {
    "should return OK and render the ReturnChangeRegistration page" in {
      given
        .commonPrecondition

      setUpForCorrectReturn(emptyUserAnswersForCorrectReturn)

      WsTestClient.withClient { client =>
        val result1 = createClientRequestGet(client, correctReturnBaseUrl + normalRoutePath)

        whenReady(result1) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title must include(Messages("You changed your soft drinks business activity - Soft Drinks Industry Levy - GOV.UK"))
        }
      }
    }
    testRequiredCorrectReturnDataMissing(correctReturnBaseUrl + normalRoutePath)
    testUnauthorisedUser(correctReturnBaseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + normalRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + normalRoutePath)
  }

  s"POST " - {
    "when user is a new packer should redirect to the Pack At Business Address Controller" in {
      given
        .commonPrecondition

      setUpForCorrectReturn(completedUserAnswersForCorrectReturnNewPackerOrImporter)
      WsTestClient.withClient { client =>
        val result = createClientRequestPOSTNoData(client, correctReturnBaseUrl + normalRoutePath, "")

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.correctReturn.routes.PackAtBusinessAddressController.onPageLoad(NormalMode).url)
        }
      }
    }

    "when user is only a new importer should redirect to the Ask Secondary Warehouse Controller" in {
      given
        .commonPrecondition

      setUpForCorrectReturn(completedUserAnswersForCorrectReturnNewPackerOrImporter.set(PackagedAsContractPackerPage, false).success.value)
      WsTestClient.withClient { client =>
        val result = createClientRequestPOSTNoData(client, correctReturnBaseUrl + normalRoutePath, "")

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.correctReturn.routes.AskSecondaryWarehouseInReturnController.onPageLoad(NormalMode).url)
        }
      }
    }

    testUnauthorisedUser(correctReturnBaseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + normalRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + normalRoutePath)
  }
}
