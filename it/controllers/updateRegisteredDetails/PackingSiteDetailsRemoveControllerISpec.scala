package controllers.updateRegisteredDetails

import controllers.ControllerITTestHelper
import models.{CheckMode, NormalMode}
import models.SelectChange.UpdateRegisteredDetails
import models.backend.Site
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.updateRegisteredDetails.PackingSiteDetailsRemovePage
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient

class PackingSiteDetailsRemoveControllerISpec extends ControllerITTestHelper {

  def normalRoutePath(index: String) = s"/packaging-site-details/remove/$index"
  def checkRoutePath(index: String) = s"/change-packaging-site-details/remove/$index"
  val indexOfPackingSiteToBeRemoved: String = "siteUNO"


  "GET UpdateRegisteredDetails - " + normalRoutePath("indexDoesntExist") - {
    "when the userAnswers contains no data" - {
      "should redirect away when no data exists" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForUpdateRegisteredDetails)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, updateRegisteredDetailsBaseUrl + normalRoutePath("indexDoesntExist"))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url)
          }
        }
      }
    }

    userAnswersForUpdateRegisteredDetailsPackingSiteDetailsRemovePage(indexOfPackingSiteToBeRemoved).foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page without the " + key + " radio checked" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, updateRegisteredDetailsBaseUrl + normalRoutePath(indexOfPackingSiteToBeRemoved))

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("updateRegisteredDetails.packingSiteDetailsRemove" + ".title"))
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe 2
              radioInputs.get(0).attr("value") mustBe "true"
              radioInputs.get(0).hasAttr("checked") mustBe false
              radioInputs.get(1).attr("value") mustBe "false"
              radioInputs.get(1).hasAttr("checked") mustBe false
              page.getElementsByClass("govuk-body-m").text() mustBe s"${ukAddress.lines.mkString(", ")} ${ukAddress.postCode}"
            }
          }
        }
      }
    }
    testUnauthorisedUser(updateRegisteredDetailsBaseUrl + normalRoutePath(indexOfPackingSiteToBeRemoved))
    testAuthenticatedUserButNoUserAnswers(updateRegisteredDetailsBaseUrl + normalRoutePath(indexOfPackingSiteToBeRemoved))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(UpdateRegisteredDetails, updateRegisteredDetailsBaseUrl + normalRoutePath(indexOfPackingSiteToBeRemoved))
  }

  s"GET " + checkRoutePath(indexOfPackingSiteToBeRemoved) - {
    "when the userAnswers contains no data" - {
      "should redirect away when no data exists" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersForUpdateRegisteredDetails)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, updateRegisteredDetailsBaseUrl + checkRoutePath("indexDoesntExist"))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(CheckMode).url)
          }
        }
      }
    }

    userAnswersForUpdateRegisteredDetailsPackingSiteDetailsRemovePage(indexOfPackingSiteToBeRemoved).foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page without the " + key + " radio checked" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, updateRegisteredDetailsBaseUrl + checkRoutePath(indexOfPackingSiteToBeRemoved))

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("updateRegisteredDetails.packingSiteDetailsRemove" + ".title"))
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

    testUnauthorisedUser(updateRegisteredDetailsBaseUrl + checkRoutePath(indexOfPackingSiteToBeRemoved))
    testAuthenticatedUserButNoUserAnswers(updateRegisteredDetailsBaseUrl + checkRoutePath(indexOfPackingSiteToBeRemoved))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(UpdateRegisteredDetails, updateRegisteredDetailsBaseUrl + checkRoutePath(indexOfPackingSiteToBeRemoved))
  }

  s"POST " + normalRoutePath(indexOfPackingSiteToBeRemoved) - {

    userAnswersForUpdateRegisteredDetailsPackingSiteDetailsRemovePage(indexOfPackingSiteToBeRemoved).foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        "should update the session with the new value and redirect to the index controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswersForUpdateRegisteredDetails)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, updateRegisteredDetailsBaseUrl + normalRoutePath(indexOfPackingSiteToBeRemoved), Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url)
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
                client, updateRegisteredDetailsBaseUrl + normalRoutePath(indexOfPackingSiteToBeRemoved), Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(NormalMode).url)
                val userAnswersAfterTest = getAnswers(userAnswers.id)
                val dataStoredForPage = userAnswersAfterTest.fold[Option[Boolean]](None)(_.get(PackingSiteDetailsRemovePage))
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
          emptyUserAnswersForUpdateRegisteredDetails
            .copy(packagingSiteList = Map(indexOfPackingSiteToBeRemoved -> Site(ukAddress, None, None, None))))
        getAnswers(emptyUserAnswersForUpdateRegisteredDetails.id).get.packagingSiteList.size mustBe 1
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, updateRegisteredDetailsBaseUrl + normalRoutePath(indexOfPackingSiteToBeRemoved), Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("updateRegisteredDetails.packingSiteDetailsRemove" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe Messages("updateRegisteredDetails.packingSiteDetailsRemove" + ".error.required")
            page.getElementsByClass("govuk-body-m").text() mustBe s"${ukAddress.lines.mkString(", ")} ${ukAddress.postCode}"
            getAnswers(emptyUserAnswersForUpdateRegisteredDetails.id).get.packagingSiteList.size mustBe 1
          }
        }
      }
    }
    testUnauthorisedUser(updateRegisteredDetailsBaseUrl + normalRoutePath(indexOfPackingSiteToBeRemoved), Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(updateRegisteredDetailsBaseUrl + normalRoutePath(indexOfPackingSiteToBeRemoved), Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(UpdateRegisteredDetails, updateRegisteredDetailsBaseUrl + normalRoutePath(indexOfPackingSiteToBeRemoved), Some(Json.obj("value" -> "true")))
  }

  s"POST " + checkRoutePath(indexOfPackingSiteToBeRemoved) - {

    userAnswersForUpdateRegisteredDetailsPackingSiteDetailsRemovePage(indexOfPackingSiteToBeRemoved).foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        "should update the session with the new value and redirect to the checkAnswers controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswersForUpdateRegisteredDetails)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, updateRegisteredDetailsBaseUrl + checkRoutePath(indexOfPackingSiteToBeRemoved), Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(CheckMode).url)
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
                client, updateRegisteredDetailsBaseUrl + checkRoutePath(indexOfPackingSiteToBeRemoved), Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.PackagingSiteDetailsController.onPageLoad(CheckMode).url)
                val userAnswersAfterTest = getAnswers(userAnswers.id)
                val dataStoredForPage = userAnswersAfterTest.fold[Option[Boolean]](None)(_.get(PackingSiteDetailsRemovePage))
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
          emptyUserAnswersForUpdateRegisteredDetails
            .copy(packagingSiteList = Map(indexOfPackingSiteToBeRemoved -> Site(ukAddress, None, None, None))))
        getAnswers(emptyUserAnswersForUpdateRegisteredDetails.id).get.packagingSiteList.size mustBe 1
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, updateRegisteredDetailsBaseUrl + checkRoutePath(indexOfPackingSiteToBeRemoved), Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            getAnswers(emptyUserAnswersForUpdateRegisteredDetails.id).get.packagingSiteList.size mustBe 1
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("updateRegisteredDetails.packingSiteDetailsRemove" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe Messages("updateRegisteredDetails.packingSiteDetailsRemove" + ".error.required")
            page.getElementsByClass("govuk-body-m").text() mustBe s"${ukAddress.lines.mkString(", ")} ${ukAddress.postCode}"
          }
        }
      }
    }
    testUnauthorisedUser(updateRegisteredDetailsBaseUrl + checkRoutePath(indexOfPackingSiteToBeRemoved), Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(updateRegisteredDetailsBaseUrl + checkRoutePath(indexOfPackingSiteToBeRemoved), Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(UpdateRegisteredDetails, updateRegisteredDetailsBaseUrl + checkRoutePath(indexOfPackingSiteToBeRemoved), Some(Json.obj("value" -> "true")))
  }
}
