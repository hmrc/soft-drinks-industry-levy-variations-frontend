package controllers.correctReturn

import controllers.ControllerITTestHelper
import models.SelectChange.CorrectReturn
import models.correctReturn.RepaymentMethod
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers._
import pages.correctReturn.{ CorrectReturnBaseCYAPage, RepaymentMethodPage }
import play.api.http.HeaderNames
import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.json.Json
import play.api.test.{ FakeRequest, WsTestClient }

class RepaymentMethodControllerISpec extends ControllerITTestHelper {
  val normalRoutePath = "/repayment-method"
  val checkRoutePath = "/change-repayment-method"

  given messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  given messages: Messages = messagesApi.preferred(FakeRequest())

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the RepaymentMethod page with no data populated" in {
        build.commonPrecondition

        setUpForCorrectReturn(emptyUserAnswersForCorrectReturn.set(CorrectReturnBaseCYAPage, true).success.value)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, correctReturnBaseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(messages("correctReturn.repaymentMethod" + ".title"))
            val radioInputs = page.getElementsByClass("govuk-radios__input")
            radioInputs.size() mustBe 2

            RepaymentMethod.values.zipWithIndex.foreach { case (radio1, index1) =>
              radioInputs.get(index1).attr("value") mustBe radio1.toString
              radioInputs.get(index1).hasAttr("checked") mustBe false
            }
          }
        }
      }
    }

    RepaymentMethod.values.zipWithIndex.foreach { case (radio, index) =>
      s"when the userAnswers contains data for the page with " + radio.toString + " selected" - {
        s"should return OK and render the page with " + radio.toString + " radio checked" in {
          build.commonPrecondition

          val userAnswers = emptyUserAnswersForCorrectReturn
            .set(CorrectReturnBaseCYAPage, true)
            .success
            .value
            .set(RepaymentMethodPage, radio)
            .success
            .value

          setUpForCorrectReturn(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, correctReturnBaseUrl + normalRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(messages("correctReturn.repaymentMethod" + ".title"))
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe RepaymentMethod.values.size

              RepaymentMethod.values.zipWithIndex.foreach { case (radio1, index1) =>
                radioInputs.get(index1).attr("value") mustBe radio1.toString
                radioInputs.get(index1).hasAttr("checked") mustBe index == index1
              }
            }
          }
        }
      }
    }
    testRequiredCorrectReturnDataMissing(correctReturnBaseUrl + normalRoutePath)
    testUnauthorisedUser(correctReturnBaseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + normalRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + normalRoutePath)
  }

  s"GET " + checkRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the RepaymentMethod page with no data populated" in {
        build.commonPrecondition

        setUpForCorrectReturn(emptyUserAnswersForCorrectReturn.set(CorrectReturnBaseCYAPage, true).success.value)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, correctReturnBaseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(messages("correctReturn.repaymentMethod" + ".title"))
            val radioInputs = page.getElementsByClass("govuk-radios__input")
            radioInputs.size() mustBe 2

            RepaymentMethod.values.zipWithIndex.foreach { case (radio1, index1) =>
              radioInputs.get(index1).attr("value") mustBe radio1.toString
              radioInputs.get(index1).hasAttr("checked") mustBe false
            }
          }
        }
      }
    }

    RepaymentMethod.values.zipWithIndex.foreach { case (radio, index) =>
      s"when the userAnswers contains data for the page with " + radio.toString + " selected" - {
        s"should return OK and render the page with " + radio.toString + " radio checked" in {
          build.commonPrecondition

          val userAnswers = emptyUserAnswersForCorrectReturn
            .set(CorrectReturnBaseCYAPage, true)
            .success
            .value
            .set(RepaymentMethodPage, radio)
            .success
            .value

          setUpForCorrectReturn(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, correctReturnBaseUrl + checkRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(messages("correctReturn.repaymentMethod" + ".title"))
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe 2

              RepaymentMethod.values.zipWithIndex.foreach { case (radio1, index1) =>
                radioInputs.get(index1).attr("value") mustBe radio1.toString
                radioInputs.get(index1).hasAttr("checked") mustBe index == index1
              }
            }
          }
        }
      }
    }
    testRequiredCorrectReturnDataMissing(correctReturnBaseUrl + checkRoutePath)
    testUnauthorisedUser(correctReturnBaseUrl + checkRoutePath)
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + checkRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + checkRoutePath)
  }

  s"POST " + normalRoutePath - {
    RepaymentMethod.values.foreach { case radio =>
      "when the user selects " + radio.toString - {
        "should update the session with the new value and redirect to the index controller" - {
          "when the session contains no data for page" in {
            build.commonPrecondition

            setUpForCorrectReturn(emptyUserAnswersForCorrectReturn.set(CorrectReturnBaseCYAPage, true).success.value)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client,
                correctReturnBaseUrl + normalRoutePath,
                Json.obj("value" -> radio)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(
                  routes.CorrectReturnCheckChangesCYAController.onPageLoad().url
                )
                val dataStoredForPage =
                  getAnswers(sdilNumber).fold[Option[RepaymentMethod]](None)(_.get(RepaymentMethodPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe radio
              }
            }
          }

          "when the session already contains data for page" in {
            build.commonPrecondition

            val userAnswers = emptyUserAnswersForCorrectReturn
              .set(CorrectReturnBaseCYAPage, true)
              .success
              .value
              .set(RepaymentMethodPage, radio)
              .success
              .value

            setUpForCorrectReturn(userAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client,
                correctReturnBaseUrl + normalRoutePath,
                Json.obj("value" -> radio)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(
                  routes.CorrectReturnCheckChangesCYAController.onPageLoad().url
                )
                val dataStoredForPage =
                  getAnswers(userAnswers.id).fold[Option[RepaymentMethod]](None)(_.get(RepaymentMethodPage))
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
        build.commonPrecondition

        setUpForCorrectReturn(emptyUserAnswersForCorrectReturn.set(CorrectReturnBaseCYAPage, true).success.value)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client,
            correctReturnBaseUrl + normalRoutePath,
            Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + messages("correctReturn.repaymentMethod" + ".title"))
            val errorSummary = page
              .getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value_0"
            errorSummary.text() mustBe "Select how you want the credit to be repaid"
          }
        }
      }
    }
    testUnauthorisedUser(correctReturnBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(
      CorrectReturn,
      correctReturnBaseUrl + normalRoutePath,
      Some(Json.obj("value" -> "true"))
    )
  }

  s"POST " + checkRoutePath - {
    RepaymentMethod.values.foreach { case radio =>
      "when the user selects " + radio.toString - {
        "should update the session with the new value and redirect to the checkAnswers controller" - {
          "when the session contains no data for page" in {
            build.commonPrecondition

            setUpForCorrectReturn(emptyUserAnswersForCorrectReturn.set(CorrectReturnBaseCYAPage, true).success.value)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client,
                correctReturnBaseUrl + checkRoutePath,
                Json.obj("value" -> Json.toJson(radio))
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(
                  routes.CorrectReturnCheckChangesCYAController.onPageLoad().url
                )
                val dataStoredForPage =
                  getAnswers(sdilNumber).fold[Option[RepaymentMethod]](None)(_.get(RepaymentMethodPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe radio
              }
            }
          }

          "when the session already contains data for page" in {
            build.commonPrecondition

            val userAnswers = emptyUserAnswersForCorrectReturn
              .set(CorrectReturnBaseCYAPage, true)
              .success
              .value
              .set(RepaymentMethodPage, radio)
              .success
              .value

            setUpForCorrectReturn(userAnswers)
            WsTestClient.withClient { client =>
              val result = createClientRequestPOST(
                client,
                correctReturnBaseUrl + checkRoutePath,
                Json.obj("value" -> Json.toJson(radio))
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(
                  routes.CorrectReturnCheckChangesCYAController.onPageLoad().url
                )
                val dataStoredForPage =
                  getAnswers(userAnswers.id).fold[Option[RepaymentMethod]](None)(_.get(RepaymentMethodPage))
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
        build.commonPrecondition

        setUpForCorrectReturn(emptyUserAnswersForCorrectReturn.set(CorrectReturnBaseCYAPage, true).success.value)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client,
            correctReturnBaseUrl + checkRoutePath,
            Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + messages("correctReturn.repaymentMethod" + ".title"))
            val errorSummary = page
              .getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value_0"
            errorSummary.text() mustBe "Select how you want the credit to be repaid"
          }
        }
      }
    }
    testUnauthorisedUser(correctReturnBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(
      CorrectReturn,
      correctReturnBaseUrl + checkRoutePath,
      Some(Json.obj("value" -> "true"))
    )
  }
}
