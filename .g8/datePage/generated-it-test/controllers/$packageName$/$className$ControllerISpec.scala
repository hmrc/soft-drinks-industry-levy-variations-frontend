package controllers.$packageName$

import controllers.ControllerITTestHelper
import models.NormalMode
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.$packageName$.$className$Page
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient
import java.time.LocalDate
import models.SelectChange.$packageName;format="cap"$

class $className$ControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/$url$"
  val checkRoutePath = "/change-$url$"

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the $className$ page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersFor$packageName;format="cap"$)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, $packageName$BaseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("$packageName$.$className;format="decap"$" + ".title"))
            val dateInputs = page.getElementsByClass("govuk-date-input__item")
            dateInputs.size() mustBe 3
            dateInputs.get(0).getElementById("value.day").hasAttr("value") mustBe false
            dateInputs.get(1).getElementById("value.month").hasAttr("value") mustBe false
            dateInputs.get(2).getElementById("value.year").hasAttr("value") mustBe false
          }
        }
      }
    }

    s"when the userAnswers contains a date for the page" - {
      s"should return OK and render the page with the date populated" in {
        given
          .commonPrecondition

        val userAnswers = emptyUserAnswersFor$packageName;format="cap"$.set($className$Page, date).success.value

        setAnswers(userAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, $packageName$BaseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("$packageName$.$className;format="decap"$" + ".title"))
            val dateInputs = page.getElementsByClass("govuk-date-input__item")
            dateInputs.size() mustBe 3
            dateInputs.get(0).getElementById("value.day").hasAttr("value") mustBe true
            dateInputs.get(0).getElementById("value.day").attr("value") mustBe day.toString
            dateInputs.get(1).getElementById("value.month").hasAttr("value") mustBe true
            dateInputs.get(1).getElementById("value.month").attr("value") mustBe month.toString
            dateInputs.get(2).getElementById("value.year").hasAttr("value") mustBe true
            dateInputs.get(2).getElementById("value.year").attr("value") mustBe year.toString
          }
        }
      }
    }

    testUnauthorisedUser($packageName$BaseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers($packageName$BaseUrl + normalRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType($packageName;format="cap"$, $packageName$BaseUrl + normalRoutePath)
  }

  "GET " + checkRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the $className$ page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersFor$packageName;format="cap"$)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, $packageName$BaseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("$packageName$.$className;format="decap"$" + ".title"))
            val dateInputs = page.getElementsByClass("govuk-date-input__item")
            dateInputs.size() mustBe 3
            dateInputs.get(0).getElementById("value.day").hasAttr("value") mustBe false
            dateInputs.get(1).getElementById("value.month").hasAttr("value") mustBe false
            dateInputs.get(2).getElementById("value.year").hasAttr("value") mustBe false
          }
        }
      }
    }

    s"when the userAnswers contains a date for the page" - {
      s"should return OK and render the page with the date populated" in {
        given
          .commonPrecondition

        val userAnswers = emptyUserAnswersFor$packageName;format="cap"$.set($className$Page, date).success.value

        setAnswers(userAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, $packageName$BaseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("$packageName$.$className;format="decap"$" + ".title"))
            val dateInputs = page.getElementsByClass("govuk-date-input__item")
            dateInputs.size() mustBe 3
            dateInputs.get(0).getElementById("value.day").hasAttr("value") mustBe true
            dateInputs.get(0).getElementById("value.day").attr("value") mustBe day.toString
            dateInputs.get(1).getElementById("value.month").hasAttr("value") mustBe true
            dateInputs.get(1).getElementById("value.month").attr("value") mustBe month.toString
            dateInputs.get(2).getElementById("value.year").hasAttr("value") mustBe true
            dateInputs.get(2).getElementById("value.year").attr("value") mustBe year.toString
          }
        }
      }
    }

    testUnauthorisedUser($packageName$BaseUrl + checkRoutePath)
    testAuthenticatedUserButNoUserAnswers($packageName$BaseUrl + checkRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType($packageName;format="cap"$, $packageName$BaseUrl + checkRoutePath)
  }

  s"POST " + normalRoutePath - {
    "when the user inserts a valid day, month and year" - {
      "should update the session with the new value and redirect to the index controller" - {
        "when the session contains no data for page" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersFor$packageName;format="cap"$)
          WsTestClient.withClient { client =>

            val result = createClientRequestPOST(
              client, $packageName$BaseUrl + normalRoutePath, validDateJson
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some($nextPage$.url)
              val dataStoredForPage = getAnswers(sdilNumber).fold[Option[LocalDate]](None)(_.get($className$Page))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe date
            }
          }
        }

        "when the session already contains data for page" in {
          given
            .commonPrecondition

          val userAnswers = emptyUserAnswersFor$packageName;format="cap"$.set($className$Page, date).success.value

          setAnswers(userAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, $packageName$BaseUrl + normalRoutePath, validDateJson
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some($nextPage$.url)
              val dataStoredForPage = getAnswers(sdilNumber).fold[Option[LocalDate]](None)(_.get($className$Page))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe date
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

          val invalidJson = Json.obj("value." + field -> value.toString)

          setAnswers(emptyUserAnswersFor$packageName;format="cap"$)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, $packageName$BaseUrl + normalRoutePath, invalidJson
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.title must include("Error: " + Messages("$packageName$.$className;format="decap"$" + ".title"))
              val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
                .first()
              errorSummary
                .select("a")
                .attr("href") mustBe "#value.day"
              errorSummary.text() mustBe Messages("$packageName$.$className;format="decap"$" + ".error.required.two", otherFields(0), otherFields(1)
              )
            }
          }
        }

        "when " + field + "is missing" in {
          given
            .commonPrecondition

          val invalidJson = dateMapExculdingField.foldLeft(Json.obj()) { (a, b) =>
            a ++ Json.obj("value." + b._1 -> b._2.toString)
          }

          setAnswers(emptyUserAnswersFor$packageName;format="cap"$)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, $packageName$BaseUrl + normalRoutePath, invalidJson
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.title must include("Error: " + Messages("$packageName$.$className;format="decap"$" + ".title"))
              val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
                .first()
              errorSummary
                .select("a")
                .attr("href") mustBe "#value.day"
              errorSummary.text() mustBe Messages("$packageName$.$className;format="decap"$" + ".error.required", field
              )
            }
          }
        }
      }

      "when all fields are missing" in {
        given
          .commonPrecondition

        val invalidJson = dateMap.foldLeft(Json.obj()) { (a, b) =>
          a ++ Json.obj("value." + b._1 -> "")
        }

        setAnswers(emptyUserAnswersFor$packageName;format="cap"$)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, $packageName$BaseUrl + normalRoutePath, invalidJson
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("$packageName$.$className;format="decap"$" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value.day"
            errorSummary.text() mustBe Messages("$packageName$.$className;format="decap"$" + ".error.required.all"
            )
          }
        }
      }

      "when all fields are present but not a valid date" in {
        given
          .commonPrecondition

        val invalidJson = dateMap.foldLeft(Json.obj()) { (a, b) =>
          a ++ Json.obj("value." + b._1 -> "30")
        }

        setAnswers(emptyUserAnswersFor$packageName;format="cap"$)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, $packageName$BaseUrl + normalRoutePath, invalidJson
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("$packageName$.$className;format="decap"$" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value.day"
            errorSummary.text() mustBe Messages("$packageName$.$className;format="decap"$" + ".error.invalid"
            )
          }
        }
      }
    }
    testUnauthorisedUser($packageName$BaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers($packageName$BaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType($packageName;format="cap"$, $packageName$BaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
  }

  s"POST " + checkRoutePath - {
    "when the user inserts a valid day, month and year" - {
      "should update the session with the new value and redirect to the index controller" - {
        "when the session contains no data for page" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersFor$packageName;format="cap"$)
          WsTestClient.withClient { client =>

            val result = createClientRequestPOST(
              client, $packageName$BaseUrl + checkRoutePath, validDateJson
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.$packageName;format="cap"$CYAController.onPageLoad.url)
              val dataStoredForPage = getAnswers(sdilNumber).fold[Option[LocalDate]](None)(_.get($className$Page))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe date
            }
          }
        }

        "when the session already contains data for page" in {
          given
            .commonPrecondition

          val userAnswers = emptyUserAnswersFor$packageName;format="cap"$.set($className$Page, date).success.value

          setAnswers(userAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, $packageName$BaseUrl + checkRoutePath, validDateJson
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.$packageName;format="cap"$CYAController.onPageLoad.url)
              val dataStoredForPage = getAnswers(sdilNumber).fold[Option[LocalDate]](None)(_.get($className$Page))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe date
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

          val invalidJson = Json.obj("value." + field -> value.toString)

          setAnswers(emptyUserAnswersFor$packageName;format="cap"$)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, $packageName$BaseUrl + checkRoutePath, invalidJson
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.title must include("Error: " + Messages("$packageName$.$className;format="decap"$" + ".title"))
              val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
                .first()
              errorSummary
                .select("a")
                .attr("href") mustBe "#value.day"
              errorSummary.text() mustBe Messages("$packageName$.$className;format="decap"$" + ".error.required.two", otherFields(0), otherFields(1)
              )
            }
          }
        }

        "when " + field + "is missing" in {
          given
            .commonPrecondition

          val invalidJson = dateMapExculdingField.foldLeft(Json.obj()) { (a, b) =>
            a ++ Json.obj("value." + b._1 -> b._2.toString)
          }

          setAnswers(emptyUserAnswersFor$packageName;format="cap"$)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, $packageName$BaseUrl + checkRoutePath, invalidJson
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.title must include("Error: " + Messages("$packageName$.$className;format="decap"$" + ".title"))
              val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
                .first()
              errorSummary
                .select("a")
                .attr("href") mustBe "#value.day"
              errorSummary.text() mustBe Messages("$packageName$.$className;format="decap"$" + ".error.required", field
              )
            }
          }
        }
      }

      "when all fields are missing" in {
        given
          .commonPrecondition

        val invalidJson = dateMap.foldLeft(Json.obj()) { (a, b) =>
          a ++ Json.obj("value." + b._1 -> "")
        }

        setAnswers(emptyUserAnswersFor$packageName;format="cap"$)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, $packageName$BaseUrl + checkRoutePath, invalidJson
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("$packageName$.$className;format="decap"$" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value.day"
            errorSummary.text() mustBe Messages("$packageName$.$className;format="decap"$" + ".error.required.all"
            )
          }
        }
      }

      "when all fields are present but not a valid date" in {
        given
          .commonPrecondition

        val invalidJson = dateMap.foldLeft(Json.obj()) { (a, b) =>
          a ++ Json.obj("value." + b._1 -> "30")
        }

        setAnswers(emptyUserAnswersFor$packageName;format="cap"$)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, $packageName$BaseUrl + checkRoutePath, invalidJson
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("$packageName$.$className;format="decap"$" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value.day"
            errorSummary.text() mustBe Messages("$packageName$.$className;format="decap"$" + ".error.invalid"
            )
          }
        }
      }
    }
    testUnauthorisedUser($packageName$BaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers($packageName$BaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType($packageName;format="cap"$, $packageName$BaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
  }
}
