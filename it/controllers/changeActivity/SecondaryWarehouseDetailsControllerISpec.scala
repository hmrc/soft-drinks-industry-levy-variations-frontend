package controllers.changeActivity

import controllers.ControllerITTestHelper
import models.SelectChange.ChangeActivity
import models.Warehouse
import models.alf.init._
import models.backend.UkAddress
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import pages.changeActivity.SecondaryWarehouseDetailsPage
import play.api.http.HeaderNames
import play.api.libs.json.{JsObject, Json}
import play.api.test.WsTestClient
import testSupport.helpers.ALFTestHelper
import viewmodels.AddressFormattingHelper

class SecondaryWarehouseDetailsControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/secondary-warehouse-details"


  "GET " - {
    "when the userAnswers contains no data (no warehouses)" - {
      "should return OK and render the SecondaryWarehouseDetails page with no data populated " +
        "(with message displaying no warehouses added)" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, changeActivityBaseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title mustBe "Change your UK warehouse details - Soft Drinks Industry Levy - GOV.UK"
            val summaryList = page.getElementsByClass("govuk-caption-m")
            summaryList.text mustBe "You do not have any registered warehouses."
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

    "GET "  - {
      "when the userAnswers contains some warehouses" - {
        val singleWarehouse = Map("1" -> Warehouse(Some("ABC Ltd"), UkAddress(List("33 Rhes Priordy"), "WR53 7CX")))
        val multipleWarehouses = singleWarehouse ++ Map("2" -> Warehouse(Some("ACME Soft Drinks"), UkAddress(List("1 Watch Street"), "DF4 3WE")))
        List(singleWarehouse, multipleWarehouses).foreach { warehouseList =>
          "should return OK and render the SecondaryWarehouseDetails page with no data populated " +
            s"(with message displaying summary list of warehouses) for warehouse list size ${warehouseList.size}" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswersForChangeActivity.copy(warehouseList = warehouseList))

            WsTestClient.withClient { client =>
              val result1 = createClientRequestGet(client, changeActivityBaseUrl + normalRoutePath)

              whenReady(result1) { res =>
                res.status mustBe 200
                val page = Jsoup.parse(res.body)
                page.title mustBe "Change your UK warehouse details - Soft Drinks Industry Levy - GOV.UK"
                val summaryList = page.getElementsByClass("govuk-caption-m")

                summaryList.text mustBe warehouseList.values
                  .map(warehouse => {
                    val removeText = s"Remove warehouse ${warehouse.tradingName.getOrElse("")} at ${warehouse.address.lines.head}"
                    val formattedAddress = AddressFormattingHelper.addressFormatting(warehouse.address, warehouse.tradingName)
                    s"${formattedAddress.toString().replace("<br>", " ")} $removeText"
                  })
                  .mkString(" ")
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
    }

    userAnswersForChangeActivitySecondaryWarehouseDetailsPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page with " + key + " radio checked" +
        "(with message displaying no warehouses added)" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, changeActivityBaseUrl + normalRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title mustBe "Change your UK warehouse details - Soft Drinks Industry Levy - GOV.UK"
              val summaryList = page.getElementsByClass("govuk-caption-m")
              summaryList.text mustBe "You do not have any registered warehouses."
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


  s"POST " - {
    "when the user selects no" - {
      "should update the session with the new value and redirect to the CYA controller" - {
        "when the session contains no data for page" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersForChangeActivity)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> "false")
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.ChangeActivityCYAController.onPageLoad.url)
              val dataStoredForPage = getAnswers(sdilNumber).fold[Option[Boolean]](None)(_.get(SecondaryWarehouseDetailsPage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe false
            }
          }
        }

        "when the session already contains data for page" in {
          given
            .commonPrecondition

          setAnswers(filledUserAnswersForChangeActivityPackagingSiteDetailsPage)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> "false")
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.ChangeActivityCYAController.onPageLoad.url)
              val dataStoredForPage = getAnswers(sdilNumber).fold[Option[Boolean]](None)(_.get(SecondaryWarehouseDetailsPage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe false
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
          accessibilityFooterUrl = None,
          phaseFeedbackLink = Some(s"http://localhost:9250/contact/beta-feedback?service=soft-drinks-industry-levy-variations-frontend&backUrl=http%3A%2F%2Flocalhost%3A8705%2Fsoft-drinks-industry-levy-variations-frontend%2Fchange-activity%2Fsecondary-warehouse-details"),
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
          pageHeadingStyle = Some("govuk-heading-m")
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
        Json.obj("changeActivity" -> Json.obj( "secondaryWarehouseDetails" -> true)
      ))

      val alfOnRampURL: String = "http://onramp.com"

      given
        .commonPrecondition
        .alf.getSuccessResponseFromALFInit(alfOnRampURL)
      setAnswers(emptyUserAnswersForChangeActivity)

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

  "when the user does not select yes or no" - {
      "should return 400 with required error" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForChangeActivity)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, changeActivityBaseUrl + normalRoutePath, Json.obj("value" -> "")
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
            errorSummary.text() mustBe "Select yes if you want to add another UK warehouse"
          }
        }
      }
    }
    testUnauthorisedUser(changeActivityBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(changeActivityBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(ChangeActivity, changeActivityBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
  }

}
