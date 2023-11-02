package controllers.correctReturn

import config.FrontendAppConfig
import controllers.ControllerITTestHelper
import models.SelectChange.CorrectReturn
import models.{LitresInBands, UserAnswers}
import models.alf.init.{AppLevelLabels, ConfirmPageConfig, EditPageLabels, JourneyConfig, JourneyLabels, JourneyOptions, LanguageLabels, LookupPageLabels, SelectPageConfig, TimeoutConfig}
import org.jsoup.Jsoup
import org.scalatest.TryValues
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import pages.correctReturn.{HowManyBroughtIntoUKPage, PackagingSiteDetailsPage}
import play.api.http.HeaderNames
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import testSupport.SDILBackendTestData.aSubscription
import testSupport.helpers.ALFTestHelper

class PackagingSiteDetailsControllerISpec extends ControllerITTestHelper with TryValues {

  lazy val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val normalRoutePath = "/packaging-site-details"
  val checkRoutePath = "/change-packaging-site-details"

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the PackagingSiteDetails page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForCorrectReturn)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, correctReturnBaseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title mustBe "Do you want to add another UK packaging site? - Soft Drinks Industry Levy - GOV.UK"
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

    userAnswersForCorrectReturnPackagingSiteDetailsPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page with " + key + " radio checked" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, correctReturnBaseUrl + normalRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title mustBe "Do you want to add another UK packaging site? - Soft Drinks Industry Levy - GOV.UK"
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe 2
              radioInputs.get(0).attr("value") mustBe "true"
              radioInputs.get(0).hasAttr("checked") mustBe key == "yes"
              radioInputs.get(1).attr("value") mustBe "false"
              radioInputs.get(1).hasAttr("checked") mustBe key == "no"
            }
          }
        }
      }
    }
    testUnauthorisedUser(correctReturnBaseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + normalRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + normalRoutePath)
  }

  s"GET " + checkRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the PackagingSiteDetails page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForCorrectReturn)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, correctReturnBaseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title mustBe "Do you want to add another UK packaging site? - Soft Drinks Industry Levy - GOV.UK"
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

    userAnswersForCorrectReturnPackagingSiteDetailsPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page with " + key + " radio checked" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, correctReturnBaseUrl + checkRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title mustBe "Do you want to add another UK packaging site? - Soft Drinks Industry Levy - GOV.UK"
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe 2
              radioInputs.get(0).attr("value") mustBe "true"
              radioInputs.get(0).hasAttr("checked") mustBe key == "yes"
              radioInputs.get(1).attr("value") mustBe "false"
              radioInputs.get(1).hasAttr("checked") mustBe key == "no"
            }
          }
        }
      }
    }

    testUnauthorisedUser(correctReturnBaseUrl + checkRoutePath)
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + checkRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + checkRoutePath)
  }

  s"POST " + normalRoutePath - {
    userAnswersForCorrectReturnPackagingSiteDetailsPage.foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        "should update the session with the new value and redirect to the index controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswersForCorrectReturn)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, correctReturnBaseUrl + normalRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              if(!yesSelected){
                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
                  val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(PackagingSiteDetailsPage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe yesSelected
                }
              }
            }
          }

          "when the session already contains data for page" in {
            given
              .commonPrecondition

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, correctReturnBaseUrl + normalRoutePath, Json.obj("value" -> yesSelected.toString)
              )
              if(!yesSelected) {
                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.CorrectReturnCheckChangesCYAController.onPageLoad.url)
                  val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(PackagingSiteDetailsPage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe yesSelected
                }
              }
            }
          }
        }
      }
    }

    "when the user does not select yes or no" - {
      "should return 400 with required error" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForCorrectReturn)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, correctReturnBaseUrl + normalRoutePath, Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title mustBe "Error: Do you want to add another UK packaging site? - Soft Drinks Industry Levy - GOV.UK"
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
    testUnauthorisedUser(correctReturnBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
  }

  "Post " - {
    "when user selected yes, user should be taken to ALF" in {
      val journeyConfigToBePosted: JourneyConfig = JourneyConfig(
        version = 2,
        options = JourneyOptions(
          continueUrl = s"http://localhost:8705/soft-drinks-industry-levy-variations-frontend/off-ramp/packing-site-details/$sdilNumber",
          homeNavHref = None,
          signOutHref = Some(controllers.auth.routes.AuthController.signOut.url),
          accessibilityFooterUrl = None,
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
          serviceHref = Some(controllers.routes.IndexController.onPageLoad.url),
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
            "email" -> "Adeline.Greene@gmail.com"), "packagingSiteDetails" -> true)))
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

  s"POST " + checkRoutePath - {

    "when the user does not select yes or no" - {
      "should return 400 with required error" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForCorrectReturn)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, correctReturnBaseUrl + checkRoutePath, Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title mustBe "Error: Do you want to add another UK packaging site? - Soft Drinks Industry Levy - GOV.UK"
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
    testUnauthorisedUser(correctReturnBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
  }
}
