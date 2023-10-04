package controllers.updateRegisteredDetails

import controllers.ControllerITTestHelper
import models.CheckMode
import models.SelectChange.UpdateRegisteredDetails
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.updateRegisteredDetails.UpdateContactDetailsPage
import play.api.http.Status.OK
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import viewmodels.AddressFormattingHelper

class UpdateRegisteredDetailsCYAControllerISpec extends ControllerITTestHelper {

  val route = "/change-registered-details/check-your-answers"
  "GET " + routes.UpdateRegisteredDetailsCYAController.onPageLoad.url - {
    "when the userAnswers contains business address only" - {
      "should render the page including uk site details summary for zero values" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForUpdateRegisteredDetails.copy(data = Json.obj(), packagingSiteList = Map.empty, warehouseList = Map.empty))

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, baseUrl + route)

          whenReady(result) { res =>
            res.status mustBe OK
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("updateRegisteredDetails.checkYourAnswers.title"))
            page.getElementsByTag("h2").get(1).text() mustBe "Business address"
            val ukSiteElems = page.getElementsByClass("govuk-summary-list").first().getElementsByClass("govuk-summary-list__row")
            ukSiteElems.get(0).getElementsByClass("govuk-summary-list__key").first().text() mustBe "You have 0 packaging sites"
            ukSiteElems.get(1).getElementsByClass("govuk-summary-list__key").first.text() mustBe "You have 0 warehouses"
            val businessAddressElems = page.getElementsByClass("govuk-summary-list").get(1).getElementsByClass("govuk-summary-list__row")
            businessAddressElems.get(0).getElementsByClass("govuk-summary-list__key").first().text() mustBe
              AddressFormattingHelper.addressFormatting(emptyUserAnswersForUpdateRegisteredDetails.contactAddress, None).toString().replace("<br>", " ")
            businessAddressElems.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change business address"
            businessAddressElems.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "business address"
            page.getElementsByClass("govuk-summary-list").size() mustBe 2
            page.getElementsByClass("govuk-summary-list__row").size() mustBe 3
          }
        }
      }
    }

    "when the userAnswers contains a packaging site" - {
      "should render the page with the packaging site details as a summary" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForUpdateRegisteredDetails.copy(data = Json.obj(), packagingSiteList = packagingSitesFromSubscription, warehouseList = Map.empty))

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, baseUrl + route)

          whenReady(result) { res =>
            res.status mustBe OK
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("updateRegisteredDetails.checkYourAnswers.title"))
            page.getElementsByTag("h2").first.text() mustBe "UK site details"
            val elems = page.getElementsByClass("govuk-summary-list").first().getElementsByClass("govuk-summary-list__row")
            elems.get(0).getElementsByClass("govuk-summary-list__key").first().text() mustBe "You have 1 packaging site"
            elems.get(1).getElementsByClass("govuk-summary-list__key").first().text() mustBe "You have 0 warehouses"
            page.getElementsByClass("govuk-summary-list").size() mustBe 2
            page.getElementsByClass("govuk-summary-list__row").size() mustBe 3
          }
        }
      }
    }

    "when the userAnswers contains a warehouse" - {
      "should render the page with the warehouse details as a summary" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForUpdateRegisteredDetails
          .copy(data = Json.obj(), packagingSiteList = Map.empty, warehouseList = Map(warehousesFromSubscription.head)))

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, baseUrl + route)

          whenReady(result) { res =>
            res.status mustBe OK
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("updateRegisteredDetails.checkYourAnswers.title"))
            page.getElementsByTag("h2").first.text() mustBe "UK site details"
            val elems = page.getElementsByClass("govuk-summary-list").first().getElementsByClass("govuk-summary-list__row")
            elems.get(0).getElementsByClass("govuk-summary-list__key").first().text() mustBe "You have 0 packaging sites"
            elems.get(1).getElementsByClass("govuk-summary-list__key").first().text() mustBe "You have 1 warehouse"
            page.getElementsByClass("govuk-summary-list").size() mustBe 2
            page.getElementsByClass("govuk-summary-list__row").size() mustBe 3
          }
        }
      }
    }

    s"when the userAnswers contains the $UpdateContactDetailsPage" - {
      "should render the page with the contact details as a summary" in {
        given
          .commonPrecondition
        setAnswers(emptyUserAnswersForUpdateRegisteredDetails
          .copy(packagingSiteList = Map.empty, warehouseList = Map.empty)
          .set(UpdateContactDetailsPage, contactDetails).success.value)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, baseUrl + route)

          whenReady(result) { res =>
            res.status mustBe OK
            val page = Jsoup.parse(res.body)

            page.title must include(Messages("updateRegisteredDetails.checkYourAnswers.title"))
            page.getElementsByTag("h2").get(1).text() mustBe "Soft Drinks Industry Levy contact"
            val elems = page.getElementsByClass("govuk-summary-list").get(1).getElementsByClass("govuk-summary-list__row")
            elems.get(0).getElementsByClass("govuk-summary-list__value").first().text() mustBe contactDetails.fullName
            elems.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change Contact Details"
            elems.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "Contact Details"
            elems.get(0).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.UpdateContactDetailsController.onPageLoad(CheckMode).url
            elems.get(1).getElementsByClass("govuk-summary-list__value").first().text() mustBe contactDetails.position
            elems.get(1).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change Contact Details"
            elems.get(1).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.UpdateContactDetailsController.onPageLoad(CheckMode).url
            elems.get(1).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "Contact Details"
            elems.get(2).getElementsByClass("govuk-summary-list__value").first().text() mustBe contactDetails.phoneNumber
            elems.get(2).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change Contact Details"
            elems.get(2).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.UpdateContactDetailsController.onPageLoad(CheckMode).url
            elems.get(2).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "Contact Details"
            elems.get(3).getElementsByClass("govuk-summary-list__value").first().text() mustBe contactDetails.email
            elems.get(3).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().text() mustBe "Change Contact Details"
            elems.get(3).getElementsByClass("govuk-summary-list__actions").first().getElementsByTag("a").first().attr("href") mustBe routes.UpdateContactDetailsController.onPageLoad(CheckMode).url
            elems.get(3).getElementsByClass("govuk-summary-list__actions").first().getElementsByClass("govuk-visually-hidden").first().text() mustBe "Contact Details"
            page.getElementsByClass("govuk-summary-list").size() mustBe 3
            page.getElementsByClass("govuk-summary-list__row").size() mustBe 7
            page.getElementsByTag("form").first().attr("action") mustBe routes.UpdateRegisteredDetailsCYAController.onSubmit.url
            page.getElementsByTag("form").first().getElementsByTag("button").first().text() mustBe "Confirm updates and send"
          }
        }
      }
    }

    testUnauthorisedUser(baseUrl + route)
    testAuthenticatedUserButNoUserAnswers(baseUrl + route)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(UpdateRegisteredDetails, baseUrl + route)
  }
  "POST " + routes.UpdateRegisteredDetailsCYAController.onSubmit.url - {
    "when the userAnswers contains no data" - {
      "should redirect to next page" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForUpdateRegisteredDetails)

        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(client, baseUrl + route, Json.obj())

          whenReady(result) { res =>
            res.header(HeaderNames.LOCATION) mustBe Some(routes.UpdateDoneController.onPageLoad.url)
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + route, Some(Json.obj()))
    testAuthenticatedUserButNoUserAnswers(baseUrl + route, Some(Json.obj()))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(UpdateRegisteredDetails, baseUrl + route, Some(Json.obj()))
  }
}