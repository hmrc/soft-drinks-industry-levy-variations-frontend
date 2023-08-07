package controllers.updateRegisteredDetails

import controllers.ControllerITTestHelper
import models.SelectChange.UpdateRegisteredDetails
import models.updateRegisteredDetails.UpdateContactDetails
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.updateRegisteredDetails.UpdateContactDetailsPage
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.test.WsTestClient

class UpdateContactDetailsControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/change-registered-details/contact-details-add"
  val checkRoutePath = "/change-registered-details/change-contact-details-add"

  val updateContactDetailsJsObject = Json.toJson(updateContactDetails).as[JsObject].value
  val updateContactDetailsMap: Map[String, String] = Map("fullName" -> "Full Name",
    "position" -> "job position",
    "phoneNumber" -> "012345678901",
    "email" -> "email@test.com")

  val fieldNameToLabel = Map("fullName" -> "Full name",
    "position" -> "Job title",
    "phoneNumber" -> "Telephone number",
    "email" -> "Email address")

  val userAnswers = emptyUserAnswersForUpdateRegisteredDetails.set(UpdateContactDetailsPage, updateContactDetails).success.value

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the UpdateContactDetails page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForUpdateRegisteredDetails.copy(data = Json.obj()))

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("updateRegisteredDetails.updateContactDetails" + ".title"))
            val inputFields = page.getElementsByClass("govuk-form-group")
            inputFields.size() mustBe 4
            updateContactDetailsMap.zipWithIndex.foreach { case ((fieldName, _), index) =>
              inputFields.get(index).text() mustBe fieldNameToLabel(fieldName)
              inputFields.get(index).getElementById(fieldName).hasAttr("value") mustBe false
            }
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
          val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("updateRegisteredDetails.updateContactDetails" + ".title"))
            val inputFields = page.getElementsByClass("govuk-form-group")
            inputFields.size() mustBe 4
            updateContactDetailsMap.zipWithIndex.foreach { case ((fieldName, fieldValue), index) =>
              inputFields.get(index).text() mustBe fieldNameToLabel(fieldName)
              inputFields.get(index).getElementById(fieldName).hasAttr("value") mustBe true
              inputFields.get(index).getElementById(fieldName).attr("value") mustBe fieldValue
            }
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(UpdateRegisteredDetails, baseUrl + normalRoutePath)
  }

  "GET " + checkRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the UpdateContactDetails page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForUpdateRegisteredDetails.copy(data = Json.obj()))

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("updateRegisteredDetails.updateContactDetails" + ".title"))
            val inputFields = page.getElementsByClass("govuk-form-group")
            inputFields.size() mustBe 4
            updateContactDetailsMap.zipWithIndex.foreach { case ((fieldName, _), index) =>
              inputFields.get(index).text() mustBe fieldNameToLabel(fieldName)
              inputFields.get(index).getElementById(fieldName).hasAttr("value") mustBe false
            }
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
          val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("updateRegisteredDetails.updateContactDetails" + ".title"))
            val inputFields = page.getElementsByClass("govuk-form-group")
            inputFields.size() mustBe 4
            updateContactDetailsMap.zipWithIndex.foreach { case ((fieldName, fieldValue), index) =>
              inputFields.get(index).text() mustBe fieldNameToLabel(fieldName)
              inputFields.get(index).getElementById(fieldName).hasAttr("value") mustBe true
              inputFields.get(index).getElementById(fieldName).attr("value") mustBe fieldValue
            }
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + checkRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(UpdateRegisteredDetails, baseUrl + checkRoutePath)
  }

  s"POST " + normalRoutePath - {
    "when the user populates answers all questions" - {
      "should update the session with the new values and redirect to the index controller" - {
        "when the session contains no data for page" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersForUpdateRegisteredDetails)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + normalRoutePath, Json.toJson(updateContactDetailsDiff)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(defaultCall.url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[UpdateContactDetails]](None)(_.get(UpdateContactDetailsPage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe updateContactDetailsDiff
            }
          }
        }

        "when the session already contains data for page" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + normalRoutePath, Json.toJson(updateContactDetailsDiff)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(defaultCall.url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[UpdateContactDetails]](None)(_.get(UpdateContactDetailsPage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe updateContactDetailsDiff
            }
          }
        }
      }
    }

    "should return 400 with required error" - {
      "when no questions are answered" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForUpdateRegisteredDetails)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, baseUrl + normalRoutePath, Json.toJson(UpdateContactDetails("", "", "", ""))
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("updateRegisteredDetails.updateContactDetails" + ".title"))
            val errorSummaryList = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first().getElementsByTag("li")
            errorSummaryList.size() mustBe updateContactDetailsMap.size
            updateContactDetailsMap.zipWithIndex.foreach { case ((fieldName, _), index) =>
              val errorSummary = errorSummaryList.get(index)
              errorSummary
                .select("a")
                .attr("href") mustBe "#" + fieldName
              errorSummary.text() mustBe Messages("updateRegisteredDetails.updateContactDetails.error." + fieldName + ".required")
            }
          }
        }
      }
      updateContactDetailsMap.zipWithIndex.foreach { case ((fieldName, _), index) =>
        "when no answer is given for field" + fieldName in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersForUpdateRegisteredDetails)
          val invalidJson = updateContactDetailsMap.foldLeft(Json.obj()) { case (current, (fn, fv)) =>
            val fieldValue = if (fn == fieldName) {
              ""
            } else {
              fv
            }
            current ++ Json.obj(fn -> fieldValue)
          }
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + normalRoutePath, invalidJson
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.title must include("Error: " + Messages("updateRegisteredDetails.updateContactDetails" + ".title"))
              val errorSummaryList = page.getElementsByClass("govuk-list govuk-error-summary__list")
                .first()
              errorSummaryList
                .select("a")
                .attr("href") mustBe "#" + fieldName
              errorSummaryList.text() mustBe Messages("updateRegisteredDetails.updateContactDetails.error." + fieldName + ".required")
            }
          }
        }
      }
    }

    testUnauthorisedUser(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(UpdateRegisteredDetails, baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
  }

  s"POST " + checkRoutePath - {
    "when the user populates answers all questions" - {
      "should update the session with the new values and redirect to the index controller" - {
        "when the session contains no data for page" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersForUpdateRegisteredDetails)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + checkRoutePath, Json.toJson(updateContactDetailsDiff)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.UpdateRegisteredDetailsCYAController.onPageLoad.url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[UpdateContactDetails]](None)(_.get(UpdateContactDetailsPage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe updateContactDetailsDiff
            }
          }
        }

        "when the session already contains data for page" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + checkRoutePath, Json.toJson(updateContactDetailsDiff)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.UpdateRegisteredDetailsCYAController.onPageLoad.url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[UpdateContactDetails]](None)(_.get(UpdateContactDetailsPage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe updateContactDetailsDiff
            }
          }
        }
      }
    }

    "should return 400 with required error" - {
      "when no questions are answered" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForUpdateRegisteredDetails)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, baseUrl + checkRoutePath, Json.toJson(UpdateContactDetails("", "", "", ""))
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("updateRegisteredDetails.updateContactDetails" + ".title"))
            val errorSummaryList = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first().getElementsByTag("li")
            errorSummaryList.size() mustBe updateContactDetailsMap.size
            updateContactDetailsMap.zipWithIndex.foreach { case ((fieldName, _), index) =>
              val errorSummary = errorSummaryList.get(index)
              errorSummary
                .select("a")
                .attr("href") mustBe "#" + fieldName
              errorSummary.text() mustBe Messages("updateRegisteredDetails.updateContactDetails.error." + fieldName + ".required")
            }
          }
        }
      }
      updateContactDetailsMap.zipWithIndex.foreach { case ((fieldName, _), index) =>
        "when no answer is given for field" + fieldName in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersForUpdateRegisteredDetails)
          val invalidJson = updateContactDetailsMap.foldLeft(Json.obj()) { case (current, (fn, fv)) =>
            val fieldValue = if (fn == fieldName) {
              ""
            } else {
              fv
            }
            current ++ Json.obj(fn -> fieldValue)
          }
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + checkRoutePath, invalidJson
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.title must include("Error: " + Messages("updateRegisteredDetails.updateContactDetails" + ".title"))
              val errorSummaryList = page.getElementsByClass("govuk-list govuk-error-summary__list")
                .first()
              errorSummaryList
                .select("a")
                .attr("href") mustBe "#" + fieldName
              errorSummaryList.text() mustBe Messages("updateRegisteredDetails.updateContactDetails.error." + fieldName + ".required")
            }
          }
        }
      }
    }

    testUnauthorisedUser(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(UpdateRegisteredDetails, baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
  }
}
