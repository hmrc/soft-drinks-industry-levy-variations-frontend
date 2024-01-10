package controllers.changeActivity

import controllers.{ControllerITTestHelper, routes}
import models.SelectChange.ChangeActivity
import models.{CheckMode, LitresInBands, NormalMode, UserAnswers}
import models.alf.init._
import models.changeActivity.AmountProduced
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.changeActivity._
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import testSupport.helpers.ALFTestHelper

class PackagingSiteDetailsControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/packaging-site-details"
  val checkRoutePath = "/change-packaging-site-details"
  val updatedUserAnswersImports: UserAnswers = emptyUserAnswersForChangeActivity
    .copy(id = sdilNumber)
    .set(AmountProducedPage, AmountProduced.None).success.value
    .set(ContractPackingPage, true).success.value
    .set(HowManyContractPackingPage, LitresInBands(1, 1)).success.value
    .set(ImportsPage, true).success.value
    .set(HowManyImportsPage, LitresInBands(1, 1)).success.value

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the PackagingSiteDetails page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, changeActivityBaseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title mustBe "UK packaging site details - Soft Drinks Industry Levy - GOV.UK"
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

    s"when the userAnswers contains an empty packaging site list" - {
      s"should return 303 and redirect to $PackAtBusinessAddressPage page" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity.copy(packagingSiteList = Map.empty))

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, changeActivityBaseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(controllers.changeActivity.routes.PackAtBusinessAddressController.onPageLoad(NormalMode).url)
          }
        }
      }
    }

    userAnswersForChangeActivityPackagingSiteDetailsPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page with neither radio checked" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, changeActivityBaseUrl + normalRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title mustBe "UK packaging site details - Soft Drinks Industry Levy - GOV.UK"
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
    testUnauthorisedUser(changeActivityBaseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + normalRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(ChangeActivity, changeActivityBaseUrl + normalRoutePath)
  }

  s"GET " + checkRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the PackagingSiteDetails page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, changeActivityBaseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title mustBe "UK packaging site details - Soft Drinks Industry Levy - GOV.UK"
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

    s"when the userAnswers contains an empty packaging site list" - {
      s"should redirect to PackAtBusinessAddress" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity.copy(packagingSiteList = Map.empty))

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, changeActivityBaseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(controllers.changeActivity.routes.PackAtBusinessAddressController.onPageLoad(CheckMode).url)
          }
        }
      }
    }

    userAnswersForChangeActivityPackagingSiteDetailsPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page with neither radio checked" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, changeActivityBaseUrl + checkRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("changeActivity.packagingSiteDetails" + ".title"))
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

    testUnauthorisedUser(changeActivityBaseUrl + checkRoutePath)
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + checkRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(ChangeActivity, changeActivityBaseUrl + checkRoutePath)
  }


  s"POST " + normalRoutePath - {
      "when the user selects no" - {
        "should redirect to the SecondaryWarehouseDetails controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(updatedUserAnswersImports)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> "false")
              )
              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(controllers.changeActivity.routes.SecondaryWarehouseDetailsController.onPageLoad(NormalMode).url)
                val dataStoredForPage = getAnswers(updatedUserAnswersImports.id).fold[Option[Boolean]](None)(_.get(PackagingSiteDetailsPage))
                dataStoredForPage mustBe Some(false)
              }
            }
          }
        }
      }

      "user selected yes, user should be taken to ALF" in {
        val journeyConfigToBePosted: JourneyConfig = JourneyConfig(
          version = 2,
          options = JourneyOptions(
            continueUrl = s"http://localhost:8705/soft-drinks-industry-levy-variations-frontend/off-ramp/packing-site-details/$sdilNumber",
            homeNavHref = None,
            signOutHref = Some(controllers.auth.routes.AuthController.signOut.url),
            accessibilityFooterUrl = None,
            phaseFeedbackLink = Some(s"http://localhost:9250/contact/beta-feedback?service=soft-drinks-industry-levy-variations-frontend&backUrl=http%3A%2F%2Flocalhost%3A8705%2Fsoft-drinks-industry-levy-variations-frontend%2Fchange-activity%2Fpackaging-site-details"),
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
          Json.obj("changeActivity" -> Json.obj("amountProduced" -> "none", "contractPacking" -> true,
            "howManyContractPacking" -> Json.obj("lowBand" -> 1, "highBand" -> 1), "imports" -> true,
            "howManyImports" -> Json.obj("lowBand" -> 1, "highBand" -> 1), "packagingSiteDetails" -> true)))

        val alfOnRampURL: String = "http://onramp.com"

        given
          .commonPrecondition
          .alf.getSuccessResponseFromALFInit(alfOnRampURL)
        setAnswers(updatedUserAnswersImports)

        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> "true")
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

    "when the user does not select yes or no" - {
      "should return 400 with required error" in {
        given
          .commonPrecondition

        setAnswers(updatedUserAnswersImports)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title mustBe "Error: UK packaging site details - Soft Drinks Industry Levy - GOV.UK"
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe "Select yes if you want to add another packaging site"
          }
        }
      }
    }
    testUnauthorisedUser(changeActivityBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(ChangeActivity, changeActivityBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))

  s"POST " + checkRoutePath - {
    "when the user selects no" - {
      "should redirect to the CYA controller" - {
        "when the session contains no data for page" in {
          given
            .commonPrecondition

          setAnswers(updatedUserAnswersImports)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, changeActivityBaseUrl + checkRoutePath, Json.obj("value" -> "false")
            )
            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(controllers.changeActivity.routes.ChangeActivityCYAController.onPageLoad.url)
              val dataStoredForPage = getAnswers(updatedUserAnswersImports.id).fold[Option[Boolean]](None)(_.get(PackagingSiteDetailsPage))
              dataStoredForPage mustBe Some(false)
            }
          }
        }
      }
    }

    "user selected yes, user should be taken to ALF" in {
      val journeyConfigToBePosted: JourneyConfig = JourneyConfig(
        version = 2,
        options = JourneyOptions(
          continueUrl = s"http://localhost:8705/soft-drinks-industry-levy-variations-frontend/off-ramp/packing-site-details/$sdilNumber",
          homeNavHref = None,
          signOutHref = Some(controllers.auth.routes.AuthController.signOut.url),
          accessibilityFooterUrl = None,
          phaseFeedbackLink = Some(s"http://localhost:9250/contact/beta-feedback?service=soft-drinks-industry-levy-variations-frontend&backUrl=http%3A%2F%2Flocalhost%3A8705%2Fsoft-drinks-industry-levy-variations-frontend%2Fchange-activity%2Fchange-packaging-site-details"),
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
        Json.obj("changeActivity" -> Json.obj("amountProduced" -> "none", "contractPacking" -> true,
          "howManyContractPacking" -> Json.obj("lowBand" -> 1, "highBand" -> 1), "imports" -> true,
          "howManyImports" -> Json.obj("lowBand" -> 1, "highBand" -> 1), "packagingSiteDetails" -> true)))

      val alfOnRampURL: String = "http://onramp.com"

      given
        .commonPrecondition
        .alf.getSuccessResponseFromALFInit(alfOnRampURL)
      setAnswers(updatedUserAnswersImports)

      WsTestClient.withClient { client =>
        val result = createClientRequestPOST(
          client, changeActivityBaseUrl + checkRoutePath, Json.obj("value" -> "true")
        )
        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(alfOnRampURL)
          getAnswers(sdilNumber).map(userAnswers => userAnswers.data) mustBe expectedResultInDB
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
            page.title mustBe "Error: UK packaging site details - Soft Drinks Industry Levy - GOV.UK"
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe "Select yes if you want to add another packaging site"
          }
        }
      }
    }
  }
}
