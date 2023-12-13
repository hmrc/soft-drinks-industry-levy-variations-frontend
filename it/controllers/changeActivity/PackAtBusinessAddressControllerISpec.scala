package controllers.changeActivity

import controllers.ControllerITTestHelper
import models.{CheckMode, NormalMode}
import models.SelectChange.ChangeActivity
import models.alf.init._
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.changeActivity.PackAtBusinessAddressPage
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient
import testSupport.helpers.ALFTestHelper

class PackAtBusinessAddressControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/pack-at-business-address"
  val checkRoutePath = "/change-pack-at-business-address"

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the PackAtBusinessAddress page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity.copy(packagingSiteList = Map.empty))

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, changeActivityBaseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("changeActivity.packAtBusinessAddress" + ".title"))
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

    userAnswersForChangeActivityPackAtBusinessAddressPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with $key selected" - {
        s"should return OK and render the PackAtBusinessAddress page with $key populated" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, changeActivityBaseUrl + normalRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("changeActivity.packAtBusinessAddress" + ".title"))
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

    testUnauthorisedUser(changeActivityBaseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + normalRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(ChangeActivity, changeActivityBaseUrl + normalRoutePath)
  }

  s"GET " + checkRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the PackAtBusinessAddress page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity.copy(packagingSiteList = Map.empty))

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, changeActivityBaseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("changeActivity.packAtBusinessAddress" + ".title"))
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

    userAnswersForChangeActivityPackAtBusinessAddressPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with $key selected" - {
        s"should return OK and render the PackAtBusinessAddress page with $key populated" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, changeActivityBaseUrl + checkRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("changeActivity.packAtBusinessAddress" + ".title"))
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

    testUnauthorisedUser(changeActivityBaseUrl + checkRoutePath)
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + checkRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(ChangeActivity, changeActivityBaseUrl + checkRoutePath)
  }

  s"POST " + normalRoutePath - {
    userAnswersForChangeActivityPackAtBusinessAddressPage.get("yes").foreach { userAnswers =>
      val key = "yes"
      "when the user selects yes" - {
        "should update the session with the new value and redirect to the packaging site details controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswersForChangeActivity.copy(packagingSiteList = Map.empty))
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url)
                val updatedAnswers = getAnswers(userAnswers.id)
                val dataStoredForPage = updatedAnswers.fold[Option[Boolean]](None)(_.get(PackAtBusinessAddressPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
                updatedAnswers.map(data => data.packagingSiteList) mustBe Some(if (yesSelected) packAtBusinessAddressSite else Map())
              }
            }
          }

          userAnswersForChangeActivityPackagingSiteDetailsPage.foreach { case (previousKey, _) =>
            s"when the session already contains $previousKey data for page" in {
              given
                .commonPrecondition

              val userAnswersWithPreviousSelection = if (previousKey == "yes") {
                userAnswers.copy(packagingSiteList = packAtBusinessAddressSite)
              } else {
                userAnswers.copy(packagingSiteList = Map.empty)
              }
              setAnswers(userAnswersWithPreviousSelection)
              WsTestClient.withClient { client =>
                val yesSelected = key == "yes"
                val result = createClientRequestPOST(
                  client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> yesSelected.toString)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url)
                  val updatedAnswers = getAnswers(userAnswers.id)
                  val dataStoredForPage = updatedAnswers.fold[Option[Boolean]](None)(_.get(PackAtBusinessAddressPage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe yesSelected
                  updatedAnswers.map(data => data.packagingSiteList) mustBe Some(if (yesSelected) packAtBusinessAddressSite else Map())
                }
              }
            }
          }
        }
      }
    }

    "user selected no, user should be taken to ALF in normal mode" in {
      val journeyConfigToBePosted: JourneyConfig = JourneyConfig(
        version = 2,
        options = JourneyOptions(
          continueUrl = s"http://localhost:8705/soft-drinks-industry-levy-variations-frontend/off-ramp/pack-at-business-address/$sdilNumber",
          homeNavHref = None,
          signOutHref = Some(controllers.auth.routes.AuthController.signOut.url),
          accessibilityFooterUrl = None,
          phaseFeedbackLink = Some(s"http://localhost:9250/contact/beta-feedback?service=soft-drinks-industry-levy-variations-frontend&backUrl=http%3A%2F%2Flocalhost%3A8705%2Fsoft-drinks-industry-levy-variations-frontend%2Fchange-activity%2Fpack-at-business-address"),
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

      val alfOnRampURL: String = "http://onramp.com"

      given
        .commonPrecondition
        .alf.getSuccessResponseFromALFInit(alfOnRampURL)
      setAnswers(emptyUserAnswersForChangeActivity)

      WsTestClient.withClient { client =>
        val result1 = createClientRequestPOST(client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> "false"))

        whenReady(result1) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(alfOnRampURL)
          ALFTestHelper.requestedBodyMatchesExpected(wireMockServer, journeyConfigToBePosted) mustBe true
        }
      }
    }

    "when the user does not select yes or no" - {
      "should return 400 with required error" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity.copy(packagingSiteList = Map.empty))
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("changeActivity.packAtBusinessAddress" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe Messages("changeActivity.packAtBusinessAddress" + ".error.required")
          }
        }
      }
    }
    testUnauthorisedUser(changeActivityBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(ChangeActivity, changeActivityBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
  }

  s"POST " + checkRoutePath - {
    userAnswersForChangeActivityPackAtBusinessAddressPage.get("yes").foreach { userAnswers =>
      val key = "yes"
      "when the user selects yes" - {
        "should update the session with the new value and redirect to the packaging site details controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswersForChangeActivity.copy(packagingSiteList = Map.empty))
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, changeActivityBaseUrl + checkRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(CheckMode).url)
                val updatedAnswers = getAnswers(userAnswers.id)
                val dataStoredForPage = updatedAnswers.fold[Option[Boolean]](None)(_.get(PackAtBusinessAddressPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
                updatedAnswers.map(data => data.packagingSiteList) mustBe Some(if (yesSelected) packAtBusinessAddressSite else Map())
              }
            }
          }

          userAnswersForChangeActivityPackagingSiteDetailsPage.foreach { case (previousKey, _) =>
            s"when the session already contains $previousKey data for page" in {
              given
                .commonPrecondition

              val userAnswersWithPreviousSelection = if (previousKey == "yes") {
                userAnswers.copy(packagingSiteList = packAtBusinessAddressSite)
              } else {
                userAnswers.copy(packagingSiteList = Map.empty)
              }
              setAnswers(userAnswersWithPreviousSelection)
              WsTestClient.withClient { client =>
                val yesSelected = key == "yes"
                val result = createClientRequestPOST(
                  client, changeActivityBaseUrl + checkRoutePath, Json.obj("value" -> yesSelected.toString)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(CheckMode).url)
                  val updatedAnswers = getAnswers(userAnswers.id)
                  val dataStoredForPage = updatedAnswers.fold[Option[Boolean]](None)(_.get(PackAtBusinessAddressPage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe yesSelected
                  updatedAnswers.map(data => data.packagingSiteList) mustBe Some(if (yesSelected) packAtBusinessAddressSite else Map())
                }
              }
            }
          }
        }
      }
    }

    "user selected no, user should be taken to ALF in check mode" in {
      val journeyConfigToBePosted: JourneyConfig = JourneyConfig(
        version = 2,
        options = JourneyOptions(
          continueUrl = s"http://localhost:8705/soft-drinks-industry-levy-variations-frontend/off-ramp/pack-at-business-address/$sdilNumber",
          homeNavHref = None,
          signOutHref = Some(controllers.auth.routes.AuthController.signOut.url),
          accessibilityFooterUrl = None,
          phaseFeedbackLink = Some(s"http://localhost:9250/contact/beta-feedback?service=soft-drinks-industry-levy-variations-frontend&backUrl=http%3A%2F%2Flocalhost%3A8705%2Fsoft-drinks-industry-levy-variations-frontend%2Fchange-activity%2Fpack-at-business-address"),
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

      val alfOnRampURL: String = "http://onramp.com"
      given
        .commonPrecondition
        .alf.getSuccessResponseFromALFInit(alfOnRampURL)

      setAnswers(emptyUserAnswersForChangeActivity)

      WsTestClient.withClient { client =>
        val result1 = createClientRequestPOST(client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> "false"))

        whenReady(result1) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(alfOnRampURL)
          ALFTestHelper.requestedBodyMatchesExpected(wireMockServer, journeyConfigToBePosted) mustBe true
        }
      }
    }

    "when the user does not select yes or no" - {
      "should return 400 with required error" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, changeActivityBaseUrl + checkRoutePath, Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("changeActivity.packAtBusinessAddress" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe Messages("changeActivity.packAtBusinessAddress" + ".error.required")
          }
        }
      }
    }
    testUnauthorisedUser(changeActivityBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(ChangeActivity, changeActivityBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
  }
}
