package controllers.updateRegisteredDetails

import controllers.ControllerITTestHelper
import models.SelectChange.{UpdateRegisteredDetails, writes}
import models.alf.init._
import models.backend.{Site, UkAddress}
import models.updateRegisteredDetails.ChangeRegisteredDetails
import models.updateRegisteredDetails.ChangeRegisteredDetails.{BusinessAddress, ContactDetails, Sites}
import models.{NormalMode, UserAnswers}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.updateRegisteredDetails.{ChangeRegisteredDetailsPage, WarehouseDetailsPage}
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.test.WsTestClient
import testSupport.helpers.ALFTestHelper

class WarehouseDetailsControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/warehouse-details"
  val checkRoutePath = "/change-warehouse-details"
  val values: Seq[ChangeRegisteredDetails] = Seq(
    Sites,
    ContactDetails,
    BusinessAddress
  )
  val selectedChangeRegisteredDetails: Seq[ChangeRegisteredDetails] = ChangeRegisteredDetails.values
  val userAnswersWithAllChangeRegisteredDetailsSelections: UserAnswers = emptyUserAnswersForUpdateRegisteredDetails
    .set(ChangeRegisteredDetailsPage, selectedChangeRegisteredDetails).success.value


  "GET " + normalRoutePath - {
    "when the userAnswers contains no data (no warehouses)" - {
      "should return OK and render the WarehouseDetails page with no data populated " +
        "(with message displaying no warehouses added) " +
        "with subheading asking if user would like to add a warehouse" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForUpdateRegisteredDetails)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, updateRegisteredDetailsBaseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("updateRegisteredDetails.warehouseDetails" + ".title"))
            val summaryList = page.getElementsByClass("govuk-caption-m")
            summaryList.text mustBe "You don't have any registered warehouses."
            val legend = page.getElementsByClass("govuk-fieldset__legend  govuk-fieldset__legend--m")
            legend.text mustBe "Do you want to add a warehouse?"
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

    "GET " + normalRoutePath - {
      "when the userAnswers contains some warehouses" - {
        "should return OK and render the WarehouseDetails page with no data populated " +
          "(with message displaying summary list of warehouses)" +
          "with subheading asking if user would like to add another warehouse" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersForUpdateRegisteredDetails.copy(warehouseList =
            Map("1" -> Site(UkAddress(List("33 Rhes Priordy"), "WR53 7CX"), Some("ABC Ltd")))))

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, updateRegisteredDetailsBaseUrl + normalRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("updateRegisteredDetails.warehouseDetails" + ".title"))
              val summaryList = page.getElementsByClass("govuk-caption-m")
              summaryList.text mustBe "ABC Ltd 33 Rhes Priordy WR53 7CX Remove warehouse ABC Ltd at 33 Rhes Priordy"
              val legend = page.getElementsByClass("govuk-fieldset__legend  govuk-fieldset__legend--m")
              legend.text mustBe "Do you want to add another warehouse?"
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

    userAnswersForUpdateRegisteredDetailsWarehouseDetailsPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page with neither radio checked" +
          "(with message displaying no warehouses added)" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, updateRegisteredDetailsBaseUrl + normalRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("updateRegisteredDetails.warehouseDetails" + ".title"))
              val summaryList = page.getElementsByClass("govuk-caption-m")
              summaryList.text mustBe "You don't have any registered warehouses."
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
      "should return OK and render the WarehouseDetails page with no data populated" +
        "(with message displaying no warehouses added)" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForUpdateRegisteredDetails)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, updateRegisteredDetailsBaseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("updateRegisteredDetails.warehouseDetails" + ".title"))
            val summaryList = page.getElementsByClass("govuk-caption-m")
            summaryList.text mustBe "You don't have any registered warehouses."
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

    userAnswersForUpdateRegisteredDetailsWarehouseDetailsPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page with neither radio checked" +
          "(with message displaying no warehouses added)" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, updateRegisteredDetailsBaseUrl + checkRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("updateRegisteredDetails.warehouseDetails" + ".title"))
              val summaryList = page.getElementsByClass("govuk-caption-m")
              summaryList.text mustBe "You don't have any registered warehouses."
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe 2
              radioInputs.get(0).attr("value") mustBe "true"
              radioInputs.get(0).hasAttr("checked") mustBe false
              radioInputs.get(1).attr("value") mustBe "false"
              radioInputs.get(1).hasAttr("checked") mustBe false
              getAnswers(sdilNumber).map(userAnswers => userAnswers.warehouseList).get mustBe Map.empty
            }
          }
        }
      }
    }

    testUnauthorisedUser(updateRegisteredDetailsBaseUrl + checkRoutePath)
    testAuthenticatedUserButNoUserAnswers(updateRegisteredDetailsBaseUrl + checkRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(UpdateRegisteredDetails, updateRegisteredDetailsBaseUrl + checkRoutePath)
  }

  val userAnswersWithSiteOnlyChangeRegisteredDetailsSelections: UserAnswers = emptyUserAnswersForUpdateRegisteredDetails
    .set(ChangeRegisteredDetailsPage, Seq(Sites)).success.value
  val userAnswersWithSitesAndContactDetailsChangeRegisteredDetailsSelections: UserAnswers = emptyUserAnswersForUpdateRegisteredDetails
    .set(ChangeRegisteredDetailsPage, Seq(Sites, ContactDetails)).success.value
  val userAnswersWithSitesAndBusinessAddressChangeRegisteredDetailsSelections: UserAnswers = emptyUserAnswersForUpdateRegisteredDetails
    .set(ChangeRegisteredDetailsPage, Seq(Sites, BusinessAddress)).success.value

  s"POST " + normalRoutePath - {

    "when the user selects no and there are no other options selected from Update Registered Details" - {
      "should update the session with the new value and redirect to the CYA controller" - {
        "when the session contains no data for page" in {
          given
            .commonPrecondition

          setAnswers(userAnswersWithSiteOnlyChangeRegisteredDetailsSelections)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, updateRegisteredDetailsBaseUrl + normalRoutePath, Json.obj("value" -> "false")
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(controllers.updateRegisteredDetails.routes.UpdateRegisteredDetailsCYAController.onPageLoad.url)
              val dataStoredForPage =
                getAnswers(userAnswersWithSiteOnlyChangeRegisteredDetailsSelections.id).fold[Option[Boolean]](None)(_.get(WarehouseDetailsPage))
              dataStoredForPage.isEmpty mustBe true
            }
          }
        }

        "when the user selects no and update contact details is selected from Update Registered Details" - {
          "should not update the session with the selected value and redirect to the Contact details controller" - {
            "when the session contains no data for page" in {
              given
                .commonPrecondition

              setAnswers(userAnswersWithSitesAndContactDetailsChangeRegisteredDetailsSelections)
              WsTestClient.withClient { client =>
                val result = createClientRequestPOST(
                  client, updateRegisteredDetailsBaseUrl + normalRoutePath, Json.obj("value" -> "false")
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe
                    Some(controllers.updateRegisteredDetails.routes.UpdateContactDetailsController.onPageLoad(NormalMode).url)
                  val dataStoredForPage = getAnswers(userAnswersWithSitesAndContactDetailsChangeRegisteredDetailsSelections.id)
                    .fold[Option[Boolean]](None)(_.get(WarehouseDetailsPage))
                  dataStoredForPage.isEmpty mustBe true
                }
              }
            }
          }
        }
      }

      "when user selected yes, user should be taken to ALF" in {
        val journeyConfigToBePosted: JourneyConfig = JourneyConfig(
          version = 2,
          options = JourneyOptions(
            continueUrl = s"http://localhost:8705/soft-drinks-industry-levy-variations-frontend/off-ramp/secondary-warehouses/$sdilNumber",
            homeNavHref = None,
            signOutHref = Some(controllers.auth.routes.AuthController.signOut.url),
            accessibilityFooterUrl = Some("http://localhost:12346"),
            phaseFeedbackLink = Some(s"http://localhost:9250/contact/beta-feedback?service=soft-drinks-industry-levy-variations-frontend&backUrl=http%3A%2F%2Flocalhost%3A8705%2Fsoft-drinks-industry-levy-variations-frontend%2Fchange-registered-details%2Fwarehouse-details"),
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
                    title = Some("Find UK warehouse address"),
                    heading = Some("Find UK warehouse address"),
                    postcodeLabel = Some("Postcode"))),
                editPageLabels = Some(
                  EditPageLabels(
                    title = Some("Enter the UK warehouse address"),
                    heading = Some("Enter the UK warehouse address"),
                    line1Label = Some("Address line 1"),
                    line2Label = Some("Address line 2"),
                    line3Label = Some("Address line 3 (optional)"),
                    townLabel = Some("Address line 4 (optional)"),
                    postcodeLabel = Some("Postcode"),
                    organisationLabel = Some("Trading name (optional)"))
                ),
                confirmPageLabels = None,
                countryPickerLabels = None
              ))
            )),
          requestedVersion = None
        )
        val expectedResultInDB: Some[JsObject] = Some(
          Json.obj("updateRegisteredDetails" -> Json.obj("updateContactDetails" -> Json.obj("fullName" -> "Ava Adams",
            "position" -> "Chief Infrastructure Agent","phoneNumber" -> "04495 206189","email" -> "Adeline.Greene@gmail.com"),
            "changeRegisteredDetails" -> Seq("sites")))
        )

    val alfOnRampURL: String = "http://onramp.com"

        given
          .commonPrecondition
          .alf.getSuccessResponseFromALFInit(alfOnRampURL)
        setAnswers(userAnswersWithSiteOnlyChangeRegisteredDetailsSelections)

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
            page.title mustBe "Error: Change your UK warehouse details - Soft Drinks Industry Levy - GOV.UK"
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe "Select yes if you want to register a UK warehouse"
          }
        }
      }
      "when the user has warehouses and does not select yes or no" - {
        "should return 400 with required error" in {
          given
            .commonPrecondition
          val userAnswersWithWarehouses = emptyUserAnswersForUpdateRegisteredDetails.copy(warehouseList = warehousesFromSubscription)
          setAnswers(userAnswersWithWarehouses)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, updateRegisteredDetailsBaseUrl + normalRoutePath, Json.obj("value" -> "")
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.title mustBe "Error: Change your UK warehouse details - Soft Drinks Industry Levy - GOV.UK"
              val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
                .first()
              errorSummary
                .select("a")
                .attr("href") mustBe "#value"
              errorSummary.text() mustBe "Select yes if you want to register another UK warehouse"
            }
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
    val userAnswers = userAnswersWithSiteOnlyChangeRegisteredDetailsSelections
    "when the user selects no and there are no other options selected from Update Registered Details" - {
      "should not update the session with the selected value and redirect to the CYA controller" - {
        "when the session contains no data for page" in {
          given
            .commonPrecondition

          setAnswers(userAnswersWithSiteOnlyChangeRegisteredDetailsSelections)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, updateRegisteredDetailsBaseUrl + normalRoutePath, Json.obj("value" -> "false")
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(controllers.updateRegisteredDetails.routes.UpdateRegisteredDetailsCYAController.onPageLoad.url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(WarehouseDetailsPage))
              dataStoredForPage.isEmpty mustBe true
            }
          }
        }
      }
    }

    "when the user selects no and update contact details is selected from Update Registered Details" - {
      "should not update the session with the selected value and redirect to the Contact details controller" - {
        "when the session contains no data for page" in {
          given
            .commonPrecondition

          setAnswers(userAnswersWithSitesAndContactDetailsChangeRegisteredDetailsSelections)

          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, updateRegisteredDetailsBaseUrl + normalRoutePath, Json.obj("value" -> "false")
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(controllers.updateRegisteredDetails.routes.UpdateContactDetailsController.onPageLoad(NormalMode).url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(WarehouseDetailsPage))
              dataStoredForPage.isEmpty mustBe true
            }
          }
        }
      }
    }

    "when the user selects no and update business address is selected from Update Registered Details" - {
      "should update the session with the new value and redirect to the Business address controller" - {
        "when the session contains no data for page" in {
          given
            .commonPrecondition

          setAnswers(userAnswersWithSitesAndBusinessAddressChangeRegisteredDetailsSelections)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, updateRegisteredDetailsBaseUrl + normalRoutePath, Json.obj("value" -> "false")
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(controllers.updateRegisteredDetails.routes.BusinessAddressController.onPageLoad().url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(WarehouseDetailsPage))
              dataStoredForPage.isEmpty mustBe true
            }
          }
        }
      }
    }

    "when the session already contains data for page" in {
      given
        .commonPrecondition

      setAnswers(userAnswers)
      WsTestClient.withClient { client =>
        val result = createClientRequestPOST(
          client, updateRegisteredDetailsBaseUrl + checkRoutePath, Json.obj("value" -> "false")
        )

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(routes.UpdateRegisteredDetailsCYAController.onPageLoad.url)
          val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get(WarehouseDetailsPage))
          dataStoredForPage.isEmpty mustBe true
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
            page.title mustBe "Error: Change your UK warehouse details - Soft Drinks Industry Levy - GOV.UK"
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe "Select yes if you want to register a UK warehouse"
          }
        }
      }
    }

    testUnauthorisedUser(updateRegisteredDetailsBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(updateRegisteredDetailsBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(UpdateRegisteredDetails, updateRegisteredDetailsBaseUrl +
      checkRoutePath, Some(Json.obj("value" -> "true")))
  }

}
