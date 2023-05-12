package controllers

import models.NormalMode
import models.$className$
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.$className$Page
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.test.WsTestClient

class $className$ControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/$className;format="decap"$"
  val checkRoutePath = "/change$className$"

  val $className;format="decap"$JsObject = Json.toJson($className;format="decap"$).as[JsObject].value
  val $className;format="decap"$Map: collection.Map[String, String] = {
    $className;format="decap"$JsObject.map { case (fName, fValue) => fName -> fValue.as[String] }
  }

  val userAnswers = emptyUserAnswers.set($className$Page, $className;format="decap"$).success.value

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the $className$ page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("$className;format="decap"$" + ".title"))
            val inputFields = page.getElementsByClass("govuk-form-group")
            inputFields.size() mustBe 2
            $className;format="decap"$Map.zipWithIndex.foreach { case ((fieldName, _), index) =>
              inputFields.get(index).text() mustBe fieldName
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
            page.title must include(Messages("$className;format="decap"$" + ".title"))
            val inputFields = page.getElementsByClass("govuk-form-group")
            inputFields.size() mustBe 2
            $className;format="decap"$Map.zipWithIndex.foreach { case ((fieldName, fieldValue), index) =>
              inputFields.get(index).text() mustBe fieldName
              inputFields.get(index).getElementById(fieldName).hasAttr("value") mustBe true
              inputFields.get(index).getElementById(fieldName).attr("value") mustBe fieldValue
            }
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

        setAnswers(emptyUserAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, baseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("$className;format="decap"$" + ".title"))
            val inputFields = page.getElementsByClass("govuk-form-group")
            inputFields.size() mustBe 2
            $className;format="decap"$Map.zipWithIndex.foreach { case ((fieldName, _), index) =>
              inputFields.get(index).text() mustBe fieldName
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
            page.title must include(Messages("$className;format="decap"$" + ".title"))
            val inputFields = page.getElementsByClass("govuk-form-group")
            inputFields.size() mustBe 2
            $className;format="decap"$Map.zipWithIndex.foreach { case ((fieldName, fieldValue), index) =>
              inputFields.get(index).text() mustBe fieldName
              inputFields.get(index).getElementById(fieldName).hasAttr("value") mustBe true
              inputFields.get(index).getElementById(fieldName).attr("value") mustBe fieldValue
            }
          }
        }
      }
    }
    testUnauthorisedUser(baseUrl + checkRoutePath)
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath)
  }

  s"POST " + normalRoutePath - {
    "when the user populates answers all questions" - {
      "should update the session with the new values and redirect to the index controller" - {
        "when the session contains no data for page" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + normalRoutePath, Json.toJson($className;format="decap"$Diff)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some($nextPage$.url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[$className$]](None)(_.get($className$Page))
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
              client, baseUrl + normalRoutePath, Json.toJson($className;format="decap"$Diff)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some($nextPage$.url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[$className$]](None)(_.get($className$Page))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe $className;format="decap"$Diff
            }
          }
        }
      }
    }

    "should return 400 with required error" - {
      "when no questions are answered" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, baseUrl + normalRoutePath, Json.toJson($className$("", ""))
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("$className;format="decap"$" + ".title"))
            val errorSummaryList = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first().getElementsByTag("li")
            errorSummaryList.size() mustBe $className;format="decap"$Map.size
            $className;format="decap"$Map.zipWithIndex.foreach { case ((fieldName, _), index) =>
              val errorSummary = errorSummaryList.get(index)
              errorSummary
                .select("a")
                .attr("href") mustBe "#" + fieldName
              errorSummary.text() mustBe Messages("$className;format="decap"$.error." + fieldName + ".required")
            }
          }
        }
      }
      $className;format="decap"$Map.zipWithIndex.foreach { case ((fieldName, _), index) =>
        "when no answer is given for field" + fieldName in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswers)
          val invalidJson = $className;format="decap"$Map.foldLeft(Json.obj()) { case (current, (fn, fv)) =>
            val fieldValue = if (fn == fieldName) {
              ""
            } else {
              fn
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
              page.title must include("Error: " + Messages("$className;format="decap"$" + ".title"))
              val errorSummaryList = page.getElementsByClass("govuk-list govuk-error-summary__list")
                .first()
              errorSummaryList
                .select("a")
                .attr("href") mustBe "#" + fieldName
              errorSummaryList.text() mustBe Messages("$className;format="decap"$.error." + fieldName + ".required")
            }
          }
        }
      }
    }

    testUnauthorisedUser(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
  }

  s"POST " + checkRoutePath - {
    "when the user populates answers all questions" - {
      "should update the session with the new values and redirect to the index controller" - {
        "when the session contains no data for page" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, baseUrl + checkRoutePath, Json.toJson($className;format="decap"$Diff)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad.url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[$className$]](None)(_.get($className$Page))
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
              client, baseUrl + checkRoutePath, Json.toJson($className;format="decap"$Diff)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckYourAnswersController.onPageLoad.url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[$className$]](None)(_.get($className$Page))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe $className;format="decap"$Diff
            }
          }
        }
      }
    }

    "should return 400 with required error" - {
      "when no questions are answered" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswers)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, baseUrl + checkRoutePath, Json.toJson($className$("", ""))
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("$className;format="decap"$" + ".title"))
            val errorSummaryList = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first().getElementsByTag("li")
            errorSummaryList.size() mustBe $className;format="decap"$Map.size
            $className;format="decap"$Map.zipWithIndex.foreach { case ((fieldName, _), index) =>
              val errorSummary = errorSummaryList.get(index)
              errorSummary
                .select("a")
                .attr("href") mustBe "#" + fieldName
              errorSummary.text() mustBe Messages("$className;format="decap"$.error." + fieldName + ".required")
            }
          }
        }
      }
      $className;format="decap"$Map.zipWithIndex.foreach { case ((fieldName, _), index) =>
        "when no answer is given for field" + fieldName in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswers)
          val invalidJson = $className;format="decap"$Map.foldLeft(Json.obj()) { case (current, (fn, fv)) =>
            val fieldValue = if (fn == fieldName) {
              ""
            } else {
              fn
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
              page.title must include("Error: " + Messages("$className;format="decap"$" + ".title"))
              val errorSummaryList = page.getElementsByClass("govuk-list govuk-error-summary__list")
                .first()
              errorSummaryList
                .select("a")
                .attr("href") mustBe "#" + fieldName
              errorSummaryList.text() mustBe Messages("$className;format="decap"$.error." + fieldName + ".required")
            }
          }
        }
      }
    }

    testUnauthorisedUser(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(baseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))  }
}
