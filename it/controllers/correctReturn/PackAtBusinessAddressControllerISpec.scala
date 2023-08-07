package controllers.correctReturn

import controllers.ControllerITTestHelper
import models.SelectChange.CorrectReturn
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.correctReturn.PackAtBusinessAddressPage
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient

class PackAtBusinessAddressControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/pack-at-business-address"
  val checkRoutePath = "/change-pack-at-business-address"

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the PackAtBusinessAddress page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForCorrectReturn)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, correctReturnBaseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("correctReturn.packAtBusinessAddress" + ".title"))
            val radioInputs = page.getElementsByClass("govuk-radios__input")
            radioInputs.size() mustBe 2
            radioInputs.get(0).attr("value") mustBe "true"
            radioInputs.get(0).hasAttr("checked") mustBe false
            radioInputs.get(1).attr("value") mustBe "false"
            radioInputs.get(1).hasAttr("checked") mustBe false
          }
        }
      }
    }

    userAnswersForCorrectReturnPackAtBusinessAddressPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page with " + key + " radio checked" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, correctReturnBaseUrl + normalRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("correctReturn.packAtBusinessAddress" + ".title"))
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe 2
              radioInputs.get(0).attr("value") mustBe "true"
              radioInputs.get(0).hasAttr("checked") mustBe key == "yes"
              radioInputs.get(1).attr("value") mustBe "false"
              radioInputs.get(1).hasAttr("checked") mustBe key == "no"
            }
          }
        }
      }
    }
    testUnauthorisedUser(correctReturnBaseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + normalRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + normalRoutePath)
  }

  s"GET " + checkRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the PackAtBusinessAddress page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForCorrectReturn)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, correctReturnBaseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("correctReturn.packAtBusinessAddress" + ".title"))
            val radioInputs = page.getElementsByClass("govuk-radios__input")
            radioInputs.size() mustBe 2
            radioInputs.get(0).attr("value") mustBe "true"
            radioInputs.get(0).hasAttr("checked") mustBe false
            radioInputs.get(1).attr("value") mustBe "false"
            radioInputs.get(1).hasAttr("checked") mustBe false
          }
        }
      }
    }

    userAnswersForCorrectReturnPackAtBusinessAddressPage.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page with " + key + " radio checked" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, correctReturnBaseUrl + checkRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("correctReturn.packAtBusinessAddress" + ".title"))
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe 2
              radioInputs.get(0).attr("value") mustBe "true"
              radioInputs.get(0).hasAttr("checked") mustBe key == "yes"
              radioInputs.get(1).attr("value") mustBe "false"
              radioInputs.get(1).hasAttr("checked") mustBe key == "no"
            }
          }
        }
      }
    }

    testUnauthorisedUser(correctReturnBaseUrl + checkRoutePath)
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + checkRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + checkRoutePath)
  }

  s"POST " + normalRoutePath - {
    userAnswersForCorrectReturnPackAtBusinessAddressPage.foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        "should update the session with the new value and redirect to the index controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswersForCorrectReturn)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, correctReturnBaseUrl + normalRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(defaultCall.url)
                val updatedAnswers = getAnswers(userAnswers.id)
                val dataStoredForPage = updatedAnswers.fold[Option[Boolean]](None)(_.get(PackAtBusinessAddressPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
                updatedAnswers.map(data => data.packagingSiteList) mustBe Some(if (yesSelected) packAtBusinessAddressSite else Map())
              }
            }
          }

          userAnswersForCorrectReturnPackagingSiteDetailsPage.foreach { case (previousKey, _) =>
            s"when the session already contains $previousKey data for page" in {
              given
                .commonPrecondition

              val userAnswersWithPreviousSelection = if (previousKey == "yes") {
                userAnswers.copy(packagingSiteList = packAtBusinessAddressSite)
              } else {
                userAnswers
              }
              setAnswers(userAnswersWithPreviousSelection)
              WsTestClient.withClient { client =>
                val yesSelected = key == "yes"
                val result = createClientRequestPOST(
                  client, correctReturnBaseUrl + normalRoutePath, Json.obj("value" -> yesSelected.toString)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(defaultCall.url)
                  val updatedAnswers = getAnswers(userAnswers.id)
                  val dataStoredForPage = updatedAnswers.fold[Option[Boolean]](None)(_.get(PackAtBusinessAddressPage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe yesSelected
                  updatedAnswers.map(data => data.packagingSiteList) mustBe Some(if (yesSelected) packAtBusinessAddressSite else Map())
                }
              }
            }
          }
        }
      }
    }

    "when the user does not select yes or no" - {
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
            page.title must include("Error: " + Messages("correctReturn.packAtBusinessAddress" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe Messages("correctReturn.packAtBusinessAddress" + ".error.required")
          }
        }
      }
    }
    testUnauthorisedUser(correctReturnBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
  }

  s"POST " + checkRoutePath - {
    userAnswersForCorrectReturnPackAtBusinessAddressPage.foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        "should update the session with the new value and redirect to the checkAnswers controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswersForCorrectReturn)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, correctReturnBaseUrl + checkRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.CorrectReturnCYAController.onPageLoad.url)
                val updatedAnswers = getAnswers(userAnswers.id)
                val dataStoredForPage = updatedAnswers.fold[Option[Boolean]](None)(_.get(PackAtBusinessAddressPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
                updatedAnswers.map(data => data.packagingSiteList) mustBe Some(if (yesSelected) packAtBusinessAddressSite else Map())
              }
            }
          }

          userAnswersForCorrectReturnPackagingSiteDetailsPage.foreach { case (previousKey, _) =>
            s"when the session already contains $previousKey data for page" in {
              given
                .commonPrecondition

              val userAnswersWithPreviousSelection = if (previousKey == "yes") {
                userAnswers.copy(packagingSiteList = packAtBusinessAddressSite)
              } else {
                userAnswers
              }
              setAnswers(userAnswersWithPreviousSelection)
              WsTestClient.withClient { client =>
                val yesSelected = key == "yes"
                val result = createClientRequestPOST(
                  client, correctReturnBaseUrl + checkRoutePath, Json.obj("value" -> yesSelected.toString)
                )

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.CorrectReturnCYAController.onPageLoad.url)
                  val updatedAnswers = getAnswers(userAnswers.id)
                  val dataStoredForPage = updatedAnswers.fold[Option[Boolean]](None)(_.get(PackAtBusinessAddressPage))
                  dataStoredForPage.nonEmpty mustBe true
                  dataStoredForPage.get mustBe yesSelected
                  updatedAnswers.map(data => data.packagingSiteList) mustBe Some(if (yesSelected) packAtBusinessAddressSite else Map())
                }
              }
            }
          }
        }
      }
    }

    "when the user does not select yes or no" - {
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
            page.title must include("Error: " + Messages("correctReturn.packAtBusinessAddress" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe Messages("correctReturn.packAtBusinessAddress" + ".error.required")
          }
        }
      }
    }
    testUnauthorisedUser(correctReturnBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
  }
}
