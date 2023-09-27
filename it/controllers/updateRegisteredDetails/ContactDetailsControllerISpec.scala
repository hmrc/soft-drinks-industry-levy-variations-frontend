package controllers.updateRegisteredDetails

import controllers.ControllerITTestHelper
import models.SelectChange.UpdateRegisteredDetails
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.updateRegisteredDetails.UpdateContactDetailsPage
import play.api.libs.json.Json
import play.api.test.WsTestClient

class ContactDetailsControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/contact-details"

  val userAnswersContainingContactAddress = emptyUserAnswersForUpdateRegisteredDetails.copy(
    data = Json.obj(
      "updateRegisteredDetails" -> Json.obj(
        UpdateContactDetailsPage.toString -> Json.obj(
          "fullName" -> "Jack Jack",
          "position" -> "Important Position",
          "phoneNumber" -> "01234567890",
          "email" -> "email@test.com"
        )
      )
    )
  )

  "GET " + normalRoutePath - {
    "should return OK and render the Contact Details page" in {
      given
        .commonPrecondition

      setAnswers(emptyUserAnswersForUpdateRegisteredDetails)

      WsTestClient.withClient { client =>
        val result1 = createClientRequestGet(client, updateRegisteredDetailsBaseUrl + normalRoutePath)

        whenReady(result1) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title mustEqual "Change person's details - Soft Drinks Industry Levy - GOV.UK"
        }
      }
    }
    testUnauthorisedUser(updateRegisteredDetailsBaseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(updateRegisteredDetailsBaseUrl + normalRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(UpdateRegisteredDetails, updateRegisteredDetailsBaseUrl + normalRoutePath)
  }

  "Page should contain the correct heading and subheading" - {
    "when contact person's details are available" in {
      given.commonPrecondition
      setAnswers(emptyUserAnswersForUpdateRegisteredDetails)

      WsTestClient.withClient { client =>
        val result = createClientRequestGet(client, updateRegisteredDetailsBaseUrl + normalRoutePath)

        whenReady(result) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.getElementsByClass("govuk-heading-l").text() mustEqual "Change person's details"
          page.getElementsByClass("govuk-body").text() mustEqual "This is the person we will contact about the Soft Drinks Industry Levy, if we need to."
        }
      }
    }
  }

  "Page should contain the correct contact details when contact person's details are available (not testing correct value in correct row here)" in {
    given.commonPrecondition
    setAnswers(emptyUserAnswersForUpdateRegisteredDetails)

    WsTestClient.withClient { client =>
      val result = createClientRequestGet(client, updateRegisteredDetailsBaseUrl + normalRoutePath)

      whenReady(result) { res =>
        res.status mustBe 200
        val page = Jsoup.parse(res.body)
        page.getElementsByClass("govuk-summary-list__key").text() must include("Full name")
        page.getElementsByClass("govuk-summary-list__key").text() must include("Job title")
        page.getElementsByClass("govuk-summary-list__key").text() must include("Telephone number")
        page.getElementsByClass("govuk-summary-list__key").text() must include("Email address")
        page.getElementsByClass("govuk-summary-list__value").text() must include("Ava Adams")
        page.getElementsByClass("govuk-summary-list__value").text() must include("Chief Infrastructure Agent")
        page.getElementsByClass("govuk-summary-list__value").text() must include("04495 206189")
        page.getElementsByClass("govuk-summary-list__value").text() must include("Adeline.Greene@gmail.com")
      }
    }
  }

  "Page should contain the correct change links and hidden content when contact person's details are available" in {
    given.commonPrecondition
    setAnswers(emptyUserAnswersForUpdateRegisteredDetails)

    WsTestClient.withClient { client =>
      val result = createClientRequestGet(client, updateRegisteredDetailsBaseUrl + normalRoutePath)

      whenReady(result) { res =>
        res.status mustBe 200
        val page = Jsoup.parse(res.body)
        page.getElementsByClass("govuk-summary-list__actions").text() must include("Change contact person's full name")
        page.getElementsByClass("govuk-summary-list__actions").text() must include("Change contact person's job title")
        page.getElementsByClass("govuk-summary-list__actions").text() must include("Change contact person's telephone number")
        page.getElementsByClass("govuk-summary-list__actions").text() must include("Change contact person's email address")
      }
    }
  }

  "Page should contain a save and continue button that allows the user to navigate to the next page" in {
    given.commonPrecondition
    setAnswers(emptyUserAnswersForUpdateRegisteredDetails)

    WsTestClient.withClient { client =>
      val result = createClientRequestGet(client, updateRegisteredDetailsBaseUrl + normalRoutePath)

      whenReady(result) { res =>
        res.status mustBe 200
        val page = Jsoup.parse(res.body)
        page.getElementsByClass("govuk-button").first().text() must include("Save and continue")
      }
    }
  }

  "Page should render existing contact details in user answers if it exists" in {
    given.commonPrecondition
    setAnswers(userAnswersContainingContactAddress)

    WsTestClient.withClient { client =>
      val result = createClientRequestGet(client, updateRegisteredDetailsBaseUrl + normalRoutePath)

      whenReady(result) { res =>
        res.status mustBe 200
        val page = Jsoup.parse(res.body)
        page.getElementsByClass("govuk-summary-list__value").text() must include("Jack Jack")
        page.getElementsByClass("govuk-summary-list__value").text() must include("Important Position")
        page.getElementsByClass("govuk-summary-list__value").text() must include("01234567890")
        page.getElementsByClass("govuk-summary-list__value").text() must include("email@test.com")
      }
    }
  }

}
