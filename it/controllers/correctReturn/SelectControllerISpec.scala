package controllers.correctReturn

import controllers.ControllerITTestHelper
import models.ReturnPeriod

import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.correctReturn.SelectPage
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient


class SelectControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/select"
  val checkRoutePath = "/change-select"

  val returnPeriodList: List[ReturnPeriod] = List(ReturnPeriod(2020, 0), ReturnPeriod(2020, 1), ReturnPeriod(2020, 2), ReturnPeriod(2020, 3),
    ReturnPeriod(2021, 0), ReturnPeriod(2021, 1), ReturnPeriod(2021, 2), ReturnPeriod(2021, 3),
    ReturnPeriod(2022, 0), ReturnPeriod(2022, 1), ReturnPeriod(2022, 2), ReturnPeriod(2022, 3))


  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the Select page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForCorrectReturn)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, correctReturnBaseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("correctReturn.select" + ".title"))
            val radioInputs = page.getElementsByClass("govuk-radios__input")
            radioInputs.size() mustBe returnPeriodList.size

            returnPeriodList.zipWithIndex.foreach { case (radio1, index1) =>
              if (index1 == 0) {
                radioInputs.get(index1).attr("value") mustBe Json.toJson(ReturnPeriod(2022, 0)).toString
                radioInputs.get(index1).hasAttr("checked") mustBe false
              }
              if (index1 == 1) {
                radioInputs.get(index1).attr("value") mustBe Json.toJson(ReturnPeriod(2022, 1)).toString
                radioInputs.get(index1).hasAttr("checked") mustBe false
              }
              if (index1 == 2) {
                radioInputs.get(index1).attr("value") mustBe Json.toJson(ReturnPeriod(2022, 2)).toString
                radioInputs.get(index1).hasAttr("checked") mustBe false
              }
              if (index1 == 3) {
                radioInputs.get(index1).attr("value") mustBe Json.toJson(ReturnPeriod(2022, 3)).toString
                radioInputs.get(index1).hasAttr("checked") mustBe false
              }
            }
          }
        }
      }
    }

    s"GET " + checkRoutePath - {
      "when the userAnswers contains no data" - {
        "should return OK and render the Select page with no data populated" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersForCorrectReturn)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, correctReturnBaseUrl + checkRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("correctReturn.select" + ".title"))
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe returnPeriodList.size
              returnPeriodList.zipWithIndex.foreach { case (radio1, index1) =>
                if (index1 == 0) {
                  radioInputs.get(index1).attr("value") mustBe Json.toJson(ReturnPeriod(2022, 0)).toString
                  radioInputs.get(index1).hasAttr("checked") mustBe false
                }
                if (index1 == 1) {
                  radioInputs.get(index1).attr("value") mustBe Json.toJson(ReturnPeriod(2022, 1)).toString
                  radioInputs.get(index1).hasAttr("checked") mustBe false
                }
                if (index1 == 2) {
                  radioInputs.get(index1).attr("value") mustBe Json.toJson(ReturnPeriod(2022, 2)).toString
                  radioInputs.get(index1).hasAttr("checked") mustBe false
                }
                if (index1 == 3) {
                  radioInputs.get(index1).attr("value") mustBe Json.toJson(ReturnPeriod(2022, 3)).toString
                  radioInputs.get(index1).hasAttr("checked") mustBe false
                }
                radioInputs.get(index1).hasAttr("checked") mustBe false
              }
            }
          }
        }
      }

      returnPeriodList.zipWithIndex.foreach { case (radio, index) =>
        s"when the userAnswers contains data for the page with " + radio.toString + " selected" - {
          s"should return OK and render the page with " + radio.toString + " radio checked" in {
            given
              .commonPrecondition

            val userAnswers = emptyUserAnswersForCorrectReturn.set(SelectPage, radio).success.value


            setAnswers(userAnswers)

            WsTestClient.withClient { client =>
              val result1 = createClientRequestGet(client, correctReturnBaseUrl + checkRoutePath)

              whenReady(result1) { res =>
                res.status mustBe 200
                val page = Jsoup.parse(res.body)
                page.title must include(Messages("correctReturn.select" + ".title"))
                val radioInputs = page.getElementsByClass("govuk-radios__input")
                radioInputs.size() mustBe returnPeriodList.size

                returnPeriodList.zipWithIndex.foreach { case (radio1, index1) =>
                  if (index1 == 0) {
                    radioInputs.get(index1).attr("value") mustBe Json.toJson(ReturnPeriod(2022, 0)).toString
                  }
                  if (index1 == 1) {
                    radioInputs.get(index1).attr("value") mustBe Json.toJson(ReturnPeriod(2022, 1)).toString
                  }
                  if (index1 == 2) {
                    radioInputs.get(index1).attr("value") mustBe Json.toJson(ReturnPeriod(2022, 2)).toString
                  }
                  if (index1 == 3) {
                    radioInputs.get(index1).attr("value") mustBe Json.toJson(ReturnPeriod(2022, 3)).toString
                  }

                }
              }
            }
          }
        }
      }
      testUnauthorisedUser(correctReturnBaseUrl + checkRoutePath)
      testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + checkRoutePath)

    }

    s"POST " + normalRoutePath - {
      returnPeriodList.foreach { case radio =>
        "when the user selects " + radio.toString - {
          "should update the session with the new value and redirect to the index controller" - {
            "when the session contains no data for page" in {
              given
                .commonPrecondition

              setAnswers(emptyUserAnswersForCorrectReturn)
              WsTestClient.withClient { client =>
                val result = createClientRequestPOST(
                  client, correctReturnBaseUrl + normalRoutePath, Json.obj("value" -> Json.toJson(radio).toString)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(defaultCall.url)
                  val dataStoredForPage = getAnswers(sdilNumber).fold[Option[ReturnPeriod]](None)(_.get(SelectPage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe radio
                }
              }
            }

            "when the session already contains data for page" in {
              given
                .commonPrecondition

              val userAnswers = emptyUserAnswersForCorrectReturn.set(SelectPage, radio).success.value

              setAnswers(userAnswers)
              WsTestClient.withClient { client =>
                val result = createClientRequestPOST(
                  client, correctReturnBaseUrl + normalRoutePath, Json.obj("value" -> Json.toJson(radio).toString)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(defaultCall.url)
                  val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[ReturnPeriod]](None)(_.get(SelectPage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe radio
                }
              }
            }
          }
        }
      }

      "when the user does not select an option" - {
        "should return 400 with required error" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersForCorrectReturn)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, correctReturnBaseUrl + normalRoutePath, Json.obj("value" -> "")
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.title must include("Error: " + Messages("correctReturn.select" + ".title"))
              val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
                .first()
              errorSummary
                .select("a")
                .attr("href") mustBe "#value_0"
              errorSummary.text() mustBe Messages("correctReturn.select" + ".error.required")
            }
          }
        }
      }
      testUnauthorisedUser(correctReturnBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
      testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    }

    s"POST " + checkRoutePath - {
      returnPeriodList.foreach { case radio =>
        "when the user selects " + radio.toString - {
          "should update the session with the new value and redirect to the checkAnswers controller" - {
            "when the session contains no data for page" in {
              given
                .commonPrecondition

              setAnswers(emptyUserAnswersForCorrectReturn)
              WsTestClient.withClient { client =>
                val result = createClientRequestPOST(
                  client, correctReturnBaseUrl + checkRoutePath, Json.obj("value" -> Json.toJson(radio).toString)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.CorrectReturnCYAController.onPageLoad.url)
                  val dataStoredForPage = getAnswers(sdilNumber).fold[Option[ReturnPeriod]](None)(_.get(SelectPage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe radio
                }
              }
            }

            "when the session already contains data for page" in {
              given
                .commonPrecondition

              val userAnswers = emptyUserAnswersForCorrectReturn.set(SelectPage, radio).success.value

              setAnswers(userAnswers)
              WsTestClient.withClient { client =>
                val result = createClientRequestPOST(
                  client, correctReturnBaseUrl + checkRoutePath, Json.obj("value" -> Json.toJson(radio).toString)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.CorrectReturnCYAController.onPageLoad.url)
                  val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[ReturnPeriod]](None)(_.get(SelectPage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe radio
                }
              }
            }
          }
        }
      }

      "when the user does not select and option" - {
        "should return 400 with required error" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersForCorrectReturn)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, correctReturnBaseUrl + checkRoutePath, Json.obj("value" -> "")
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.title must include("Error: " + Messages("correctReturn.select" + ".title"))
              val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
                .first()
              errorSummary
                .select("a")
                .attr("href") mustBe "#value_0"
              errorSummary.text() mustBe Messages("correctReturn.select" + ".error.required")
            }
          }
        }
      }
      testUnauthorisedUser(correctReturnBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
      testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    }
  }
}
