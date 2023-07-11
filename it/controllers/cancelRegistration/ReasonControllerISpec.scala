package controllers.cancelRegistration

import controllers.ControllerITTestHelper
import models.SelectChange.CancelRegistration
import models.UserAnswers
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.cancelRegistration.ReasonPage
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient

import scala.util.Random

class ReasonControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/reason"
  val checkRoutePath = "/change-reason"

  val reason = "testing123"
  val reasonDiff = "testing456"

  val randomStringExceedingMaxLength: String = Random.nextString(255 + 1)

  val userAnswers: UserAnswers = emptyUserAnswersForCancelRegistration.set(ReasonPage, reason).success.value

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the Reason page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForCancelRegistration)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, cancelRegistrationBaseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("Why do you need to cancel your registration? - Soft Drinks Industry Levy - GOV.UK"))
            val inputFields = page.getElementsByClass("govuk-textarea")
            inputFields.text() mustEqual ""
          }
        }
      }
    }

    s"when the userAnswers contains data for the page" - {
      s"should return OK and render the page with fields populated" in {
        given
          .commonPrecondition

        setAnswers(userAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, cancelRegistrationBaseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("Why do you need to cancel your registration? - Soft Drinks Industry Levy - GOV.UK"))
            val inputFields = page.getElementsByClass("govuk-textarea")
            inputFields.size() mustBe 1
            inputFields.text() mustBe reason
          }
        }
      }
    }
    testUnauthorisedUser(cancelRegistrationBaseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(cancelRegistrationBaseUrl + normalRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CancelRegistration, cancelRegistrationBaseUrl + normalRoutePath)
  }

  "GET " + checkRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the Reason page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForCancelRegistration)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, cancelRegistrationBaseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("Why do you need to cancel your registration? - Soft Drinks Industry Levy - GOV.UK"))
            val inputFields = page.getElementsByClass("govuk-textarea")
            inputFields.size() mustBe 1
            inputFields.text() mustBe ""
          }
        }
      }
    }

    s"when the userAnswers contains data for the page" - {
      s"should return OK and render the page with fields populated" in {
        given
          .commonPrecondition

        setAnswers(userAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, cancelRegistrationBaseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("Why do you need to cancel your registration? - Soft Drinks Industry Levy - GOV.UK"))
            val inputFields = page.getElementsByClass("govuk-textarea")
            inputFields.size() mustBe 1
            inputFields.text() mustBe reason
          }
        }
      }
    }
    testUnauthorisedUser(cancelRegistrationBaseUrl + checkRoutePath)
    testAuthenticatedUserButNoUserAnswers(cancelRegistrationBaseUrl + checkRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CancelRegistration, cancelRegistrationBaseUrl + checkRoutePath)
  }

  s"POST " + normalRoutePath - {
    "when the user answers the question" - {
      "should update the session with the new values and redirect to the index controller" - {
        "when the session contains no data for page" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersForCancelRegistration)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, cancelRegistrationBaseUrl + normalRoutePath, Json.obj("value" -> reasonDiff)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(defaultCall.url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[String]](None)(_.get(ReasonPage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe reasonDiff
            }
          }
        }

        "when the session already contains data for page" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, cancelRegistrationBaseUrl + normalRoutePath, Json.obj("value" -> reasonDiff)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(defaultCall.url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[String]](None)(_.get(ReasonPage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe reasonDiff
            }
          }
        }
      }
    }

    "should return 400 with required error" - {
      "when the question is not answered" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForCancelRegistration)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, cancelRegistrationBaseUrl + normalRoutePath, Json.obj("value" -> randomStringExceedingMaxLength)
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("cancelRegistration.reason" + ".title"))
            val errorSummaryList = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first().getElementsByTag("li")
            errorSummaryList.size() mustBe 1
            val errorSummary = errorSummaryList.get(0)
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe "The reason you are cancelling your registration must be 255 characters or less"
          }
        }
      }
    }
    testUnauthorisedUser(cancelRegistrationBaseUrl + normalRoutePath, Some(Json.obj("value" -> reasonDiff)))
    testAuthenticatedUserButNoUserAnswers(cancelRegistrationBaseUrl + normalRoutePath, Some(Json.obj("value" -> reasonDiff)))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CancelRegistration, cancelRegistrationBaseUrl + normalRoutePath, Some(Json.obj("value" -> reasonDiff)))
  }

  s"POST " + checkRoutePath - {
    "when the user answers the question" - {
      "should update the session with the new values and redirect to the index controller" - {
        "when the session contains no data for page" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersForCancelRegistration)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, cancelRegistrationBaseUrl + checkRoutePath, Json.obj("value" -> reasonDiff)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.CancelRegistrationCYAController.onPageLoad.url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[String]](None)(_.get(ReasonPage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe reasonDiff
            }
          }
        }

        "when the session already contains data for page" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, cancelRegistrationBaseUrl + checkRoutePath, Json.obj("value" -> reasonDiff)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.CancelRegistrationCYAController.onPageLoad.url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[String]](None)(_.get(ReasonPage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe reasonDiff
            }
          }
        }
      }
    }

    "should return 400 with required error" - {
      "when the question is not answered" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForCancelRegistration)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, cancelRegistrationBaseUrl + checkRoutePath, Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("cancelRegistration.reason" + ".title"))
            val errorSummaryList = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first().getElementsByTag("li")
            errorSummaryList.size() mustBe 1
            val errorSummary = errorSummaryList.get(0)
              errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe "Enter why you need to cancel your registration"
          }
        }
      }
    }
    testUnauthorisedUser(cancelRegistrationBaseUrl + checkRoutePath, Some(Json.obj("value" -> reasonDiff)))
    testAuthenticatedUserButNoUserAnswers(cancelRegistrationBaseUrl + checkRoutePath, Some(Json.obj("value" -> reasonDiff)))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CancelRegistration, cancelRegistrationBaseUrl + checkRoutePath, Some(Json.obj("value" -> reasonDiff)))
  }
}
