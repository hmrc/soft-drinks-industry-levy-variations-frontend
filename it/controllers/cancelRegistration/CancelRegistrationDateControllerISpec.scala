package controllers.cancelRegistration

import controllers.ControllerITTestHelper
import models.SelectChange.CancelRegistration
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.cancelRegistration.CancelRegistrationDatePage
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient

import java.time.LocalDate
import scala.util.Random


class CancelRegistrationDateControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/date"
  val checkRoutePath = "/change-date"
  val random = new Random()
  val validCancellationDate = LocalDate.now().plusDays(random.nextLong(13))
  val validCancellationDateJson = Json.obj(
    "cancelRegistrationDate.day" -> validCancellationDate.getDayOfMonth.toString,
    "cancelRegistrationDate.month" -> validCancellationDate.getMonth.getValue.toString,
    "cancelRegistrationDate.year" -> validCancellationDate.getYear.toString
  )

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the CancelRegistrationDate page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForCancelRegistration)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, cancelRegistrationBaseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("cancelRegistration.cancelRegistrationDate" + ".title"))
            val dateInputs = page.getElementsByClass("govuk-date-input__item")
            dateInputs.size() mustBe 3
            dateInputs.get(0).getElementById("cancelRegistrationDate.day").hasAttr("cancelRegistrationDate") mustBe false
            dateInputs.get(1).getElementById("cancelRegistrationDate.month").hasAttr("cancelRegistrationDate") mustBe false
            dateInputs.get(2).getElementById("cancelRegistrationDate.year").hasAttr("cancelRegistrationDate") mustBe false
          }
        }
      }
    }

    s"when the userAnswers contains a date for the page" - {
      s"should return OK and render the page with the date populated" in {
        given
          .commonPrecondition

        val userAnswers = emptyUserAnswersForCancelRegistration.set(CancelRegistrationDatePage, date).success.value

        setAnswers(userAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, cancelRegistrationBaseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("cancelRegistration.cancelRegistrationDate" + ".title"))
            val dateInputs = page.getElementsByClass("govuk-date-input__item")
            dateInputs.size() mustBe 3
            dateInputs.get(0).getElementById("cancelRegistrationDate.day").hasAttr("value") mustBe true
            dateInputs.get(0).getElementById("cancelRegistrationDate.day").attr("value") mustBe day.toString
            dateInputs.get(1).getElementById("cancelRegistrationDate.month").hasAttr("value") mustBe true
            dateInputs.get(1).getElementById("cancelRegistrationDate.month").attr("value") mustBe month.toString
            dateInputs.get(2).getElementById("cancelRegistrationDate.year").hasAttr("value") mustBe true
            dateInputs.get(2).getElementById("cancelRegistrationDate.year").attr("value") mustBe year.toString
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
      "should return OK and render the CancelRegistrationDate page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForCancelRegistration)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, cancelRegistrationBaseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("cancelRegistration.cancelRegistrationDate" + ".title"))
            val dateInputs = page.getElementsByClass("govuk-date-input__item")
            dateInputs.size() mustBe 3
            dateInputs.get(0).getElementById("cancelRegistrationDate.day").hasAttr("value") mustBe false
            dateInputs.get(1).getElementById("cancelRegistrationDate.month").hasAttr("value") mustBe false
            dateInputs.get(2).getElementById("cancelRegistrationDate.year").hasAttr("value") mustBe false
          }
        }
      }
    }

    s"when the userAnswers contains a date for the page" - {
      s"should return OK and render the page with the date populated" in {
        given
          .commonPrecondition

        val userAnswers = emptyUserAnswersForCancelRegistration.set(CancelRegistrationDatePage, date).success.value

        setAnswers(userAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, cancelRegistrationBaseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("cancelRegistration.cancelRegistrationDate" + ".title"))
            val dateInputs = page.getElementsByClass("govuk-date-input__item")
            dateInputs.size() mustBe 3
            dateInputs.get(0).getElementById("cancelRegistrationDate.day").hasAttr("value") mustBe true
            dateInputs.get(0).getElementById("cancelRegistrationDate.day").attr("value") mustBe day.toString
            dateInputs.get(1).getElementById("cancelRegistrationDate.month").hasAttr("value") mustBe true
            dateInputs.get(1).getElementById("cancelRegistrationDate.month").attr("value") mustBe month.toString
            dateInputs.get(2).getElementById("cancelRegistrationDate.year").hasAttr("value") mustBe true
            dateInputs.get(2).getElementById("cancelRegistrationDate.year").attr("value") mustBe year.toString
          }
        }
      }
    }

    testUnauthorisedUser(cancelRegistrationBaseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(cancelRegistrationBaseUrl + normalRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CancelRegistration, cancelRegistrationBaseUrl + checkRoutePath)
  }

  s"POST " + normalRoutePath - {
    "when the user inserts a valid day, month and year" - {
      "should update the session with the new value and redirect to the index controller" - {
        "when the session contains no data for page" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersForCancelRegistration)
          WsTestClient.withClient { client =>

            val result = createClientRequestPOST(
              client, cancelRegistrationBaseUrl + normalRoutePath, validCancellationDateJson
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(defaultCall.url)
              val dataStoredForPage = getAnswers(sdilNumber).fold[Option[LocalDate]](None)(_.get(CancelRegistrationDatePage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe validCancellationDate
            }
          }
        }

        "when the session already contains data for page" in {
          given
            .commonPrecondition

          val userAnswers = emptyUserAnswersForCancelRegistration.set(CancelRegistrationDatePage, date).success.value

          setAnswers(userAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, cancelRegistrationBaseUrl + normalRoutePath, validCancellationDateJson
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(defaultCall.url)
              val dataStoredForPage = getAnswers(sdilNumber).fold[Option[LocalDate]](None)(_.get(CancelRegistrationDatePage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe validCancellationDate
            }
          }
        }
      }
    }

    "should return 400 with the correct error" - {
      dateMap.foreach { case (field, value) =>
        val dateMapExculdingField = dateMap.removed(field)
        val otherFields = dateMapExculdingField.keys.toArray

        "when only the " + field + " is populated" in {
          given
            .commonPrecondition

          val invalidJson = Json.obj("cancelRegistrationDate." + field -> value.toString)

          setAnswers(emptyUserAnswersForCancelRegistration)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, cancelRegistrationBaseUrl + normalRoutePath, invalidJson
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.title must include("Error: " + Messages("cancelRegistration.cancelRegistrationDate" + ".title"))
              val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
                .first()
              errorSummary
                .select("a")
                .attr("href") mustBe "#cancelRegistrationDate.day"
              errorSummary.text() mustBe Messages("cancelRegistrationDate" + ".error.required.two", otherFields(0), otherFields(1)
              )
            }
          }
        }

        "when " + field + "is missing" in {
          given
            .commonPrecondition

          val invalidJson = dateMapExculdingField.foldLeft(Json.obj()) { (a, b) =>
            a ++ Json.obj("cancelRegistrationDate." + b._1 -> b._2.toString)
          }

          setAnswers(emptyUserAnswersForCancelRegistration)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, cancelRegistrationBaseUrl + normalRoutePath, invalidJson
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.title must include("Error: " + Messages("cancelRegistration.cancelRegistrationDate" + ".title"))
              val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
                .first()
              errorSummary
                .select("a")
                .attr("href") mustBe "#cancelRegistrationDate.day"
              errorSummary.text() mustBe Messages("cancelRegistrationDate" + ".error.required", field
              )
            }
          }
        }
      }

      "when all fields are missing" in {
        given
          .commonPrecondition

        val invalidJson = dateMap.foldLeft(Json.obj()) { (a, b) =>
          a ++ Json.obj("cancelRegistrationDate." + b._1 -> "")
        }

        setAnswers(emptyUserAnswersForCancelRegistration)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, cancelRegistrationBaseUrl + normalRoutePath, invalidJson
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("cancelRegistration.cancelRegistrationDate" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#cancelRegistrationDate.day"
            errorSummary.text() mustBe Messages("cancelRegistrationDate" + ".error.required.all"
            )
          }
        }
      }

      "when all fields are present but not a valid date" in {
        given
          .commonPrecondition

        val invalidJson = dateMap.foldLeft(Json.obj()) { (a, b) =>
          a ++ Json.obj("cancelRegistrationDate." + b._1 -> "30")
        }

        setAnswers(emptyUserAnswersForCancelRegistration)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, cancelRegistrationBaseUrl + normalRoutePath, invalidJson
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("cancelRegistration.cancelRegistrationDate" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#cancelRegistrationDate.day"
            errorSummary.text() mustBe "The date you are cancelling your registration must be a real date, like 31 7 2020"
          }
        }
      }
    }
    testUnauthorisedUser(cancelRegistrationBaseUrl + normalRoutePath, Some(Json.obj("cancelRegistrationDate" -> "true")))
    testAuthenticatedUserButNoUserAnswers(cancelRegistrationBaseUrl + normalRoutePath, Some(Json.obj("cancelRegistrationDate" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CancelRegistration, cancelRegistrationBaseUrl + normalRoutePath, Some(Json.obj("cancelRegistrationDate" -> "true")))
  }

  s"POST " + checkRoutePath - {
    "when the user inserts a valid day, month and year" - {
      "should update the session with the new value and redirect to the index controller" - {
        "when the session contains no data for page" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersForCancelRegistration)
          WsTestClient.withClient { client =>

            val result = createClientRequestPOST(
              client, cancelRegistrationBaseUrl + checkRoutePath, validCancellationDateJson
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.CancelRegistrationCYAController.onPageLoad.url)
              val dataStoredForPage = getAnswers(sdilNumber).fold[Option[LocalDate]](None)(_.get(CancelRegistrationDatePage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe validCancellationDate
            }
          }
        }

        "when the session already contains data for page" in {
          given
            .commonPrecondition

          val userAnswers = emptyUserAnswersForCancelRegistration.set(CancelRegistrationDatePage, date).success.value

          setAnswers(userAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, cancelRegistrationBaseUrl + checkRoutePath, validCancellationDateJson
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.CancelRegistrationCYAController.onPageLoad.url)
              val dataStoredForPage = getAnswers(sdilNumber).fold[Option[LocalDate]](None)(_.get(CancelRegistrationDatePage))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe validCancellationDate
            }
          }
        }
      }
    }

    "should return 400 with the correct error" - {
      dateMap.foreach { case (field, value) =>
        val dateMapExculdingField = dateMap.removed(field)
        val otherFields = dateMapExculdingField.keys.toArray

        "when only the " + field + "is populated" in {
          given
            .commonPrecondition

          val invalidJson = Json.obj("cancelRegistrationDate." + field -> value.toString)

          setAnswers(emptyUserAnswersForCancelRegistration)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, cancelRegistrationBaseUrl + checkRoutePath, invalidJson
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.title must include("Error: " + Messages("cancelRegistration.cancelRegistrationDate" + ".title"))
              val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
                .first()
              errorSummary
                .select("a")
                .attr("href") mustBe "#cancelRegistrationDate.day"
              errorSummary.text() mustBe Messages("cancelRegistrationDate" + ".error.required.two", otherFields(0), otherFields(1)
              )
            }
          }
        }

        "when " + field + "is missing" in {
          given
            .commonPrecondition

          val invalidJson = dateMapExculdingField.foldLeft(Json.obj()) { (a, b) =>
            a ++ Json.obj("cancelRegistrationDate." + b._1 -> b._2.toString)
          }

          setAnswers(emptyUserAnswersForCancelRegistration)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, cancelRegistrationBaseUrl + checkRoutePath, invalidJson
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.title must include("Error: " + Messages("cancelRegistration.cancelRegistrationDate" + ".title"))
              val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
                .first()
              errorSummary
                .select("a")
                .attr("href") mustBe "#cancelRegistrationDate.day"
              errorSummary.text() mustBe Messages("cancelRegistrationDate" + ".error.required", field
              )
            }
          }
        }
      }

      "when all fields are missing" in {
        given
          .commonPrecondition

        val invalidJson = dateMap.foldLeft(Json.obj()) { (a, b) =>
          a ++ Json.obj("cancelRegistrationDate." + b._1 -> "")
        }

        setAnswers(emptyUserAnswersForCancelRegistration)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, cancelRegistrationBaseUrl + checkRoutePath, invalidJson
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("cancelRegistration.cancelRegistrationDate" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#cancelRegistrationDate.day"
            errorSummary.text() mustBe Messages("cancelRegistrationDate" + ".error.required.all"
            )
          }
        }
      }

      "when all fields are present but not a valid date" in {
        given
          .commonPrecondition

        val invalidJson = dateMap.foldLeft(Json.obj()) { (a, b) =>
          a ++ Json.obj("cancelRegistrationDate." + b._1 -> "30")
        }

        setAnswers(emptyUserAnswersForCancelRegistration)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, cancelRegistrationBaseUrl + checkRoutePath, invalidJson
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("cancelRegistration.cancelRegistrationDate" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#cancelRegistrationDate.day"
            errorSummary.text() mustBe "The date you are cancelling your registration must be a real date, like 31 7 2020"
          }
        }
      }
    }
    testUnauthorisedUser(cancelRegistrationBaseUrl + normalRoutePath, Some(Json.obj("cancelRegistrationDate" -> "true")))
    testAuthenticatedUserButNoUserAnswers(cancelRegistrationBaseUrl + normalRoutePath, Some(Json.obj("cancelRegistrationDate" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CancelRegistration, cancelRegistrationBaseUrl + checkRoutePath, Some(Json.obj("cancelRegistrationDate" -> "true")))
  }
}
