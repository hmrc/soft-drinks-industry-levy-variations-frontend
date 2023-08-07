package controllers.correctReturn

import controllers.ControllerITTestHelper
import models.SelectChange.CorrectReturn
import models.backend.Site
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.correctReturn.RemovePackagingSiteConfirmPage
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient

class RemovePackagingSiteDetailsControllerISpec extends ControllerITTestHelper {

  def normalRoutePath(index: String) = s"/packaging-site-details/remove/$index"
  def checkRoutePath(index: String) = s"/change-packaging-site-details/remove/$index"
  val indexOfPackingSiteToBeRemoved: String = "siteUNO"

  "GET " + normalRoutePath("indexDoesntExist") - {
    "when the userAnswers contains no data" - {
      "should redirect away when no data exists" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForCorrectReturn)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, correctReturnBaseUrl + normalRoutePath("indexDoesntExist"))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.IndexController.onPageLoad.url)
          }
        }
      }
    }

    userAnswersForCorrectReturnRemovePackagingSiteConfirmPage(indexOfPackingSiteToBeRemoved).foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page without the " + key + " radio checked" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, correctReturnBaseUrl + normalRoutePath(indexOfPackingSiteToBeRemoved))

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)

              page.title must include(Messages("correctReturn.removePackagingSiteConfirm" + ".title"))
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe 2
              radioInputs.get(0).attr("value") mustBe "true"
              radioInputs.get(0).hasAttr("checked") mustBe false
              radioInputs.get(1).attr("value") mustBe "false"
              radioInputs.get(1).hasAttr("checked") mustBe false
              page.getElementById("value-hint").text() mustBe s"${ukAddress.lines.mkString(", ")} ${ukAddress.postCode}"
            }
          }
        }
      }
    }
    testUnauthorisedUser(correctReturnBaseUrl + normalRoutePath(indexOfPackingSiteToBeRemoved))
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + normalRoutePath(indexOfPackingSiteToBeRemoved))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + normalRoutePath(indexOfPackingSiteToBeRemoved))
  }

  s"GET " + checkRoutePath(indexOfPackingSiteToBeRemoved) - {
    "when the userAnswers contains no data" - {
      "should redirect away when no data exists" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForCorrectReturn)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, correctReturnBaseUrl + checkRoutePath("indexDoesntExist"))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.IndexController.onPageLoad.url)
          }
        }
      }
    }

    userAnswersForCorrectReturnRemovePackagingSiteConfirmPage(indexOfPackingSiteToBeRemoved).foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page without the " + key + " radio checked" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, correctReturnBaseUrl + checkRoutePath(indexOfPackingSiteToBeRemoved))

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("correctReturn.removePackagingSiteConfirm" + ".title"))
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
    }

    testUnauthorisedUser(correctReturnBaseUrl + checkRoutePath(indexOfPackingSiteToBeRemoved))
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + checkRoutePath(indexOfPackingSiteToBeRemoved))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + checkRoutePath(indexOfPackingSiteToBeRemoved))
  }

  s"POST " + normalRoutePath(indexOfPackingSiteToBeRemoved) - {

    userAnswersForCorrectReturnRemovePackagingSiteConfirmPage(indexOfPackingSiteToBeRemoved).foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        "should update the session with the new value and redirect to the index controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswersForCorrectReturn)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, correctReturnBaseUrl + normalRoutePath(indexOfPackingSiteToBeRemoved), Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.IndexController.onPageLoad.url)
              }
            }
          }

          "when the session already contains data for page" in {
            given
              .commonPrecondition

            setAnswers(userAnswers)
            getAnswers(userAnswers.id).get.packagingSiteList.size mustBe 1
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, correctReturnBaseUrl + normalRoutePath(indexOfPackingSiteToBeRemoved), Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.IndexController.onPageLoad.url)
                val userAnswersAfterTest = getAnswers(userAnswers.id)
                val dataStoredForPage = userAnswersAfterTest.fold[Option[Boolean]](None)(_.get(RemovePackagingSiteConfirmPage))
                if(yesSelected) {
                  userAnswersAfterTest.get.packagingSiteList.size mustBe 0
                } else {
                  userAnswersAfterTest.get.packagingSiteList.size mustBe 1
                }
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
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

        setAnswers(
          emptyUserAnswersForCorrectReturn
            .copy(packagingSiteList = Map(indexOfPackingSiteToBeRemoved -> Site(ukAddress, None, None, None))))
        getAnswers(emptyUserAnswersForCorrectReturn.id).get.packagingSiteList.size mustBe 1
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, correctReturnBaseUrl + normalRoutePath(indexOfPackingSiteToBeRemoved), Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("correctReturn.removePackagingSiteConfirm" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe Messages("correctReturn.removePackagingSiteConfirm" + ".error.required")
            page.getElementById("value-hint").text() mustBe s"${ukAddress.lines.mkString(", ")} ${ukAddress.postCode}"
            getAnswers(emptyUserAnswersForCorrectReturn.id).get.packagingSiteList.size mustBe 1
          }
        }
      }
    }
    testUnauthorisedUser(correctReturnBaseUrl + normalRoutePath(indexOfPackingSiteToBeRemoved), Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + normalRoutePath(indexOfPackingSiteToBeRemoved), Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + normalRoutePath(indexOfPackingSiteToBeRemoved), Some(Json.obj("value" -> "true")))
  }

  s"POST " + checkRoutePath(indexOfPackingSiteToBeRemoved) - {

    userAnswersForCorrectReturnRemovePackagingSiteConfirmPage(indexOfPackingSiteToBeRemoved).foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        "should update the session with the new value and redirect to the index controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswersForCorrectReturn)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, correctReturnBaseUrl + checkRoutePath(indexOfPackingSiteToBeRemoved), Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.IndexController.onPageLoad.url)
              }
            }
          }

          "when the session already contains data for page" in {
            given
              .commonPrecondition

            setAnswers(userAnswers)
            getAnswers(userAnswers.id).get.packagingSiteList.size mustBe 1
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, correctReturnBaseUrl + checkRoutePath(indexOfPackingSiteToBeRemoved), Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.IndexController.onPageLoad.url)
                val userAnswersAfterTest = getAnswers(userAnswers.id)
                val dataStoredForPage = userAnswersAfterTest.fold[Option[Boolean]](None)(_.get(RemovePackagingSiteConfirmPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
                if(yesSelected) {
                  userAnswersAfterTest.get.packagingSiteList.size mustBe 0
                } else {
                  userAnswersAfterTest.get.packagingSiteList.size mustBe 1
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

        setAnswers(
          emptyUserAnswersForCorrectReturn
            .copy(packagingSiteList = Map(indexOfPackingSiteToBeRemoved -> Site(ukAddress, None, None, None))))
        getAnswers(emptyUserAnswersForCorrectReturn.id).get.packagingSiteList.size mustBe 1
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, correctReturnBaseUrl + checkRoutePath(indexOfPackingSiteToBeRemoved), Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            getAnswers(emptyUserAnswersForCorrectReturn.id).get.packagingSiteList.size mustBe 1
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("correctReturn.removePackagingSiteConfirm" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe Messages("correctReturn.removePackagingSiteConfirm" + ".error.required")
            page.getElementById("value-hint").text() mustBe s"${ukAddress.lines.mkString(", ")} ${ukAddress.postCode}"
          }
        }
      }
    }
    testUnauthorisedUser(correctReturnBaseUrl + checkRoutePath(indexOfPackingSiteToBeRemoved), Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(correctReturnBaseUrl + checkRoutePath(indexOfPackingSiteToBeRemoved), Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(CorrectReturn, correctReturnBaseUrl + checkRoutePath(indexOfPackingSiteToBeRemoved), Some(Json.obj("value" -> "true")))
  }
}
