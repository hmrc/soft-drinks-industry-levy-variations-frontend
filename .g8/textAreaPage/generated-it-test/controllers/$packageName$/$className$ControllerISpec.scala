package controllers.$packageName$

import controllers.ControllerITTestHelper
import models.NormalMode
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.$packageName$.$className$Page
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.test.WsTestClient
import scala.util.Random

class $className$ControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/$className;format="decap"$"
  val checkRoutePath = "/change$className$"

  val $className;format="decap"$ = "testing123"
  val $className;format="decap"$Diff = "testing456"

  val randomStringExceedingMaxLength = Random.nextString($maxLength$ + 1)

  val userAnswers = emptyUserAnswersFor$packageName;format="cap"$.set($className$Page, $className;format="decap"$).success.value

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the $className$ page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersFor$packageName;format="cap"$)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("$packageName$.$className;format="decap"$" + ".title"))
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
          val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("$packageName$.$className;format="decap"$" + ".title"))
            val inputFields = page.getElementsByClass("govuk-textarea")
            inputFields.size() mustBe 1
            inputFields.text() mustBe $className;format="decap"$
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath)
  }

  "GET " + checkRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the $className$ page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersFor$packageName;format="cap"$)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("$packageName$.$className;format="decap"$" + ".title"))
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
          val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("$packageName$.$className;format="decap"$" + ".title"))
            val inputFields = page.getElementsByClass("govuk-textarea")
            inputFields.size() mustBe 1
            inputFields.text() mustBe $className;format = "decap"$
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + checkRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath)
  }

  s"POST " + normalRoutePath - {
    "when the user ansers the question" - {
      "should update the session with the new values and redirect to the index controller" - {
        "when the session contains no data for page" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersFor$packageName;format="cap"$)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + normalRoutePath, Json.obj("value" -> $className;format="decap"$Diff)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some($nextPage$.url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[String]](None)(_.get($className$Page))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe $className;format="decap"$Diff
            }
          }
        }

        "when the session already contains data for page" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + normalRoutePath, Json.obj("value" -> $className;format="decap"$Diff)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some($nextPage$.url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[String]](None)(_.get($className$Page))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe $className;format="decap"$Diff
            }
          }
        }
      }
    }

    "should return 400 with required error" - {
      "when the question is not answered" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersFor$packageName;format="cap"$)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, baseUrl + normalRoutePath, Json.obj("value" -> randomStringExceedingMaxLength)
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("$className;format="
            decap"$" + ".title"
            ) )
            val errorSummaryList = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first().getElementsByTag("li")
            errorSummaryList.size() mustBe 1
            val errorSummary = errorSummaryList.get(0)
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe Messages("$className;format="
            decap"$.error.length"
            )
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + normalRoutePath, Some(Json.obj("value" -> $className;format="decap"$Diff)))
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath, Some(Json.obj("value" -> $className;format="decap"$Diff)))
  }

  s"POST " + checkRoutePath - {
    "when the user ansers the question" - {
      "should update the session with the new values and redirect to the index controller" - {
        "when the session contains no data for page" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersFor$packageName;format="cap"$)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + checkRoutePath, Json.obj("value" -> $className;format="decap"$Diff)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.$packageName;format="cap"$CYAController.onPageLoad.url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[String]](None)(_.get($className$Page))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe $className;format="decap"$Diff
            }
          }
        }

        "when the session already contains data for page" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + checkRoutePath, Json.obj("value" -> $className;format="decap"$Diff)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.$packageName;format="cap"$CYAController.onPageLoad.url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[String]](None)(_.get($className$Page))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe $className;format="decap"$Diff
            }
          }
        }
      }
    }

    "should return 400 with required error" - {
      "when the question is not answered" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersFor$packageName;format="cap"$)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, baseUrl + checkRoutePath, Json.obj("value" -> randomStringExceedingMaxLength)
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("$packageName$.$className;format="decap"$" + ".title"))
            val errorSummaryList = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first().getElementsByTag("li")
            errorSummaryList.size() mustBe 1
            val errorSummary = errorSummaryList.get(0)
              errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe Messages("$packageName$.$className;format="decap"$.error.length")
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + checkRoutePath, Some(Json.obj("value" -> $className;format="decap"$Diff)))
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath, Some(Json.obj("value" -> $className;format="decap"$Diff)))
  }
}
