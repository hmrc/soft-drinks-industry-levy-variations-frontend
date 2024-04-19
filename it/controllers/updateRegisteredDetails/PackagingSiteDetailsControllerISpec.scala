package controllers.updateRegisteredDetails

import controllers.ControllerITTestHelper
import models.NormalMode
import models.SelectChange.UpdateRegisteredDetails
import models.alf.init._
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import pages.updateRegisteredDetails.PackagingSiteDetailsPage
import play.api.http.HeaderNames
import play.api.libs.json.{JsObject, Json}
import play.api.test.WsTestClient
import testSupport.helpers.ALFTestHelper

class PackagingSiteDetailsControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/packaging-site-details"
  val checkRoutePath = "/change-packaging-site-details"

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the PackagingSiteDetails page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForUpdateRegisteredDetails)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, updateRegisteredDetailsBaseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title mustBe "Change your UK packaging site details - Soft Drinks Industry Levy - GOV.UK"
            val radioInputs = page.getElementsByClass("govuk-radios__input")
            radioInputs.size() mustBe 2
            radioInputs.get(0).attr("value") mustBe "true"
            radioInputs.get(0).hasAttr("checked") mustBe false
            radioInputs.get(1).attr("value") mustBe "false"
            radioInputs.get(1).hasAttr("checked") mustBe false
          }
        }
      }
    }

    userAnswersForUpdateRegisteredDetailsPackagingSiteDetailsPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page with neither radio checked" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, updateRegisteredDetailsBaseUrl + normalRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title mustBe "Change your UK packaging site details - Soft Drinks Industry Levy - GOV.UK"
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe 2
              radioInputs.get(0).attr("value") mustBe "true"
              radioInputs.get(0).hasAttr("checked") mustBe false
              radioInputs.get(1).attr("value") mustBe "false"
              radioInputs.get(1).hasAttr("checked") mustBe false
            }
          }
        }
      }
    }
    testUnauthorisedUser(updateRegisteredDetailsBaseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(updateRegisteredDetailsBaseUrl + normalRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(UpdateRegisteredDetails, updateRegisteredDetailsBaseUrl + normalRoutePath)
  }

  s"GET " + checkRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the PackagingSiteDetails page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForUpdateRegisteredDetails)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, updateRegisteredDetailsBaseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title mustBe "Change your UK packaging site details - Soft Drinks Industry Levy - GOV.UK"
            val radioInputs = page.getElementsByClass("govuk-radios__input")
            radioInputs.size() mustBe 2
            radioInputs.get(0).attr("value") mustBe "true"
            radioInputs.get(0).hasAttr("checked") mustBe false
            radioInputs.get(1).attr("value") mustBe "false"
            radioInputs.get(1).hasAttr("checked") mustBe false
          }
        }
      }
    }

    userAnswersForUpdateRegisteredDetailsPackagingSiteDetailsPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page with neither radio checked" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, updateRegisteredDetailsBaseUrl + checkRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title mustBe "Change your UK packaging site details - Soft Drinks Industry Levy - GOV.UK"
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe 2
              radioInputs.get(0).attr("value") mustBe "true"
              radioInputs.get(0).hasAttr("checked") mustBe false
              radioInputs.get(1).attr("value") mustBe "false"
              radioInputs.get(1).hasAttr("checked") mustBe false
            }
          }
        }
      }
    }

    testUnauthorisedUser(updateRegisteredDetailsBaseUrl + checkRoutePath)
    testAuthenticatedUserButNoUserAnswers(updateRegisteredDetailsBaseUrl + checkRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(UpdateRegisteredDetails, updateRegisteredDetailsBaseUrl + checkRoutePath)
  }

  s"POST " + normalRoutePath - {
      "when the user selects no " - {
        "should update the session with the new value and redirect warehouse details controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswersForUpdateRegisteredDetails)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, updateRegisteredDetailsBaseUrl + normalRoutePath, Json.obj("value" -> "false")
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.WarehouseDetailsController.onPageLoad(NormalMode).url)
                val dataStoredForPage = getAnswers(emptyUserAnswersForUpdateRegisteredDetails.id).fold[Option[Boolean]](None)(_.get(PackagingSiteDetailsPage))
                dataStoredForPage.isEmpty mustBe true
              }
            }
          }

          "when the session already contains data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswersForUpdateRegisteredDetails)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, updateRegisteredDetailsBaseUrl + normalRoutePath, Json.obj("value" -> "false")
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.WarehouseDetailsController.onPageLoad(NormalMode).url)
                val dataStoredForPage = getAnswers(emptyUserAnswersForUpdateRegisteredDetails.id).fold[Option[Boolean]](None)(_.get(PackagingSiteDetailsPage))
                dataStoredForPage.isEmpty mustBe true
              }
            }
          }
        }
      }

    "when the user does not select yes or no" - {
      "should return 400 with required error" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForUpdateRegisteredDetails)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, updateRegisteredDetailsBaseUrl + normalRoutePath, Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title mustBe "Error: Change your UK packaging site details - Soft Drinks Industry Levy - GOV.UK"
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe "Select yes if you want to add another UK packaging site"
          }
        }
      }
    }
    testUnauthorisedUser(updateRegisteredDetailsBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(updateRegisteredDetailsBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(UpdateRegisteredDetails, updateRegisteredDetailsBaseUrl +
      normalRoutePath, Some(Json.obj("value" -> "true")))
  }

  s"POST " + checkRoutePath - {

      "when the user selects no "  - {
        "should update the session with the new value and redirect to the checkAnswers controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswersForUpdateRegisteredDetails)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, updateRegisteredDetailsBaseUrl + checkRoutePath, Json.obj("value" -> "false")
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.UpdateRegisteredDetailsCYAController.onPageLoad.url)
                val dataStoredForPage = getAnswers(emptyUserAnswersForUpdateRegisteredDetails.id).fold[Option[Boolean]](None)(_.get(PackagingSiteDetailsPage))
                dataStoredForPage.isEmpty mustBe true
              }
            }
          }

          "when the session already contains data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswersForUpdateRegisteredDetails)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, updateRegisteredDetailsBaseUrl + checkRoutePath, Json.obj("value" -> "false")
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.UpdateRegisteredDetailsCYAController.onPageLoad.url)
                val dataStoredForPage = getAnswers(emptyUserAnswersForUpdateRegisteredDetails.id).fold[Option[Boolean]](None)(_.get(PackagingSiteDetailsPage))
                dataStoredForPage.isEmpty mustBe true
              }
            }
          }
        }
      }

    "when the user does not select yes or no" - {
      "should return 400 with required error" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForUpdateRegisteredDetails)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, updateRegisteredDetailsBaseUrl + checkRoutePath, Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title mustBe "Error: Change your UK packaging site details - Soft Drinks Industry Levy - GOV.UK"
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe "Select yes if you want to add another UK packaging site"
          }
        }
      }
    }
    testUnauthorisedUser(updateRegisteredDetailsBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(updateRegisteredDetailsBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(UpdateRegisteredDetails, updateRegisteredDetailsBaseUrl +
      checkRoutePath, Some(Json.obj("value" -> "true")))
  }

  "Post " - {
    "when user selected yes, user should be taken to ALF" in {
      val journeyConfigToBePosted: JourneyConfig = JourneyConfig(
        version = 2,
        options = JourneyOptions(
          continueUrl = s"http://localhost:8705/soft-drinks-industry-levy-variations-frontend/off-ramp/packing-site-details/$sdilNumber",
          homeNavHref = None,
          signOutHref = Some(controllers.auth.routes.AuthController.signOut.url),
          accessibilityFooterUrl = Some("localhost/accessibility-statement/soft-drinks-industry-levy-variations-frontend"),
          phaseFeedbackLink = Some(s"http://localhost:9250/contact/beta-feedback?service=soft-drinks-industry-levy-variations-frontend&backUrl=http%3A%2F%2Flocalhost%3A8705%2Fsoft-drinks-industry-levy-variations-frontend%2Fchange-registered-details%2Fpackaging-site-details"),
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
            timeoutKeepAliveUrl = Some(controllers.routes.KeepAliveController.keepAlive.url)
          )),
         serviceHref = Some("http://localhost:8707/soft-drinks-industry-levy-account-frontend/home"),
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
                  title = Some("Find UK packaging site address"),
                  heading = Some("Find UK packaging site address"),
                  postcodeLabel = Some("Postcode"))),
              editPageLabels = Some(
                EditPageLabels(
                  title = Some("Enter the UK packaging site address"),
                  heading = Some("Enter the UK packaging site address"),
                  line1Label = Some("Address line 1"),
                  line2Label = Some("Address line 2"),
                  line3Label = Some("Address line 3 (optional)"),
                  townLabel = Some("Address line 4 (optional)"),
                  postcodeLabel = Some("Postcode"),
                  organisationLabel = Some("Packaging site name (optional)"))
              ),
              confirmPageLabels = None,
              countryPickerLabels = None
            ))
          )),
        requestedVersion = None
      )
      val expectedResultInDB: Some[JsObject] = Some(
        Json.obj(("updateRegisteredDetails", Json.obj("updateContactDetails" ->
          Json.obj("fullName" -> "Ava Adams","position" -> "Chief Infrastructure Agent","phoneNumber" -> "04495 206189",
            "email" -> "Adeline.Greene@gmail.com"))))
      )

      val alfOnRampURL: String = "https://onramp.com"

      given
        .commonPrecondition
        .alf.getSuccessResponseFromALFInit(alfOnRampURL)
      setAnswers(emptyUserAnswersForUpdateRegisteredDetails)

      WsTestClient.withClient { client =>
        val result = createClientRequestPOST(
          client, updateRegisteredDetailsBaseUrl + normalRoutePath, Json.obj("value" -> "true")
        )

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(alfOnRampURL)
          getAnswers(sdilNumber).map(userAnswers => userAnswers.data) mustBe expectedResultInDB
          ALFTestHelper.requestedBodyMatchesExpected(wireMockServer, journeyConfigToBePosted) mustBe true
        }
      }
    }
  }

}
