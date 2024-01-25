package controllers.updateRegisteredDetails

import controllers.{ControllerITTestHelper, routes}
import models.SelectChange.UpdateRegisteredDetails
import models.alf.init._
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.http.HeaderNames
import play.api.http.Status.SEE_OTHER
import play.api.libs.json.Json
import play.api.test.WsTestClient
import testSupport.helpers.ALFTestHelper

class BusinessAddressControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/business-address"
  val changeBusinessAddressPath = "/change-business-address"

  "GET " + changeBusinessAddressPath - {
    "should redirect to ALF" in {
      val journeyConfigToBePosted: JourneyConfig = JourneyConfig(
        version = 2,
        options = JourneyOptions(
          continueUrl = s"http://localhost:8705/soft-drinks-industry-levy-variations-frontend/off-ramp/business-address/$sdilNumber",
          homeNavHref = None,
          signOutHref = Some(controllers.auth.routes.AuthController.signOut.url),
          accessibilityFooterUrl = Some("localhost/accessibility-statement/soft-drinks-industry-levy-variations-frontend"),
          phaseFeedbackLink = Some(s"http://localhost:9250/contact/beta-feedback?service=soft-drinks-industry-levy-variations-frontend&backUrl=http%3A%2F%2Flocalhost%3A8705%2Fsoft-drinks-industry-levy-variations-frontend%2Fchange-registered-details%2Fchange-business-address"),
          deskProServiceName = None,
          showPhaseBanner = Some(false),
          alphaPhase = Some(false),
          includeHMRCBranding = Some(true),
          ukMode = Some(true),
          selectPageConfig = Some(SelectPageConfig(
            proposalListLimit = Some(10),
            showSearchAgainLink = Some(true)
          )),
          showBackButtons = Some(true),
          disableTranslations = Some(true),
          allowedCountryCodes = None,
          confirmPageConfig = Some(ConfirmPageConfig(
            showSearchAgainLink = Some(true),
            showSubHeadingAndInfo = Some(true),
            showChangeLink = Some(true),
            showConfirmChangeText = Some(true)
          )),
          timeoutConfig = Some(TimeoutConfig(
            timeoutAmount = 900,
            timeoutUrl = controllers.auth.routes.AuthController.signOut.url,
            timeoutKeepAliveUrl = Some(routes.KeepAliveController.keepAlive.url)
          )),
          serviceHref = Some(routes.IndexController.onPageLoad.url),
          pageHeadingStyle = Some("govuk-heading-l")
        ),
        labels = Some(
          JourneyLabels(
            en = Some(LanguageLabels(
              appLevelLabels = Some(AppLevelLabels(
                navTitle = Some("Soft Drinks Industry Levy"),
                phaseBannerHtml = None
              )),
              selectPageLabels = None,
              lookupPageLabels = Some(
                LookupPageLabels(
                  title = Some("Find UK contact address"),
                  heading = Some("Find UK contact address"),
                  postcodeLabel = Some("Postcode"))),
              editPageLabels = Some(
                EditPageLabels(
                  title = Some("Update your registered business address for the Soft Drinks Industry Levy"),
                  heading = Some("Update your registered business address for the Soft Drinks Industry Levy"),
                  line1Label = Some("Address line 1"),
                  line2Label = Some("Address line 2"),
                  line3Label = Some("Address line 3 (optional)"),
                  townLabel = Some("Address line 4 (optional)"),
                  postcodeLabel = Some("Postcode")
              )),
              confirmPageLabels = None,
              countryPickerLabels = None
            ))
          )),
        requestedVersion = None
      )

      val alfOnRampURL: String = "http://onramp.com"
      given
        .commonPrecondition
        .alf.getSuccessResponseFromALFInit(alfOnRampURL)

      given
        .commonPrecondition

      setAnswers(emptyUserAnswersForUpdateRegisteredDetails)

      WsTestClient.withClient { client =>
        val result1 = createClientRequestGet(client, updateRegisteredDetailsBaseUrl + changeBusinessAddressPath)

        whenReady(result1) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(alfOnRampURL)
          ALFTestHelper.requestedBodyMatchesExpected(wireMockServer, journeyConfigToBePosted) mustBe true
        }
      }
    }

    testUnauthorisedUser(updateRegisteredDetailsBaseUrl + changeBusinessAddressPath)
    testAuthenticatedUserButNoUserAnswers(updateRegisteredDetailsBaseUrl + changeBusinessAddressPath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(UpdateRegisteredDetails, updateRegisteredDetailsBaseUrl + changeBusinessAddressPath)
  }
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
  "POST " + normalRoutePath - {
    "should return OK and render the BusinessAddress page" in {
      given
        .commonPrecondition

      setAnswers(emptyUserAnswersForUpdateRegisteredDetails)

      WsTestClient.withClient { client =>
        val result1 = createClientRequestPOST(client, updateRegisteredDetailsBaseUrl + normalRoutePath, Json.obj("value" -> "true"))

        whenReady(result1) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.updateRegisteredDetails.routes.UpdateRegisteredDetailsCYAController.onPageLoad.url)
        }
      }
    }
  }
  testUnauthorisedUser(updateRegisteredDetailsBaseUrl + normalRoutePath,  Some(Json.obj("value" -> "true")))
  testAuthenticatedUserButNoUserAnswers(updateRegisteredDetailsBaseUrl + normalRoutePath,  Some(Json.obj("value" -> "true")))
  testAuthenticatedWithUserAnswersForUnsupportedJourneyType(UpdateRegisteredDetails,
    updateRegisteredDetailsBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
}
