package controllers.updateRegisteredDetails

import controllers.ControllerITTestHelper
import models.SelectChange.UpdateRegisteredDetails
import models.backend.Site
import models.{CheckMode, NormalMode}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers._
import pages.updateRegisteredDetails.RemoveWarehouseDetailsPage
import play.api.http.HeaderNames
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.test.{WsTestClient, FakeRequest}

class RemoveWarehouseDetailsControllerISpec extends ControllerITTestHelper {

  def normalRoutePath(index: String) = s"/warehouse-details/remove/$index"
  def checkRoutePath(index: String) = s"/change-warehouse-details/remove/$index"
  val indexOfWarehouseToBeRemoved: String = "warehouseUNO"

  given messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  given messages: Messages = messagesApi.preferred(FakeRequest())
  
  "GET " + normalRoutePath("indexDoesntExist") - {
    "when the userAnswers contains no data" - {
      "should redirect away when no data exists" in {
        build
          .commonPrecondition

        setAnswers(emptyUserAnswersForUpdateRegisteredDetails)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, updateRegisteredDetailsBaseUrl + normalRoutePath("indexDoesntExist"))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.WarehouseDetailsController.onPageLoad(NormalMode).url)
          }
        }
      }
    }

    userAnswersForUpdateRegisteredDetailsRemoveWarehouseDetailsPage(indexOfWarehouseToBeRemoved).foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page without the " + key + " radio checked" in {
          build
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, updateRegisteredDetailsBaseUrl + normalRoutePath(indexOfWarehouseToBeRemoved))

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(messages("updateRegisteredDetails.removeWarehouseDetails" + ".title"))
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
    testUnauthorisedUser(updateRegisteredDetailsBaseUrl + normalRoutePath(indexOfWarehouseToBeRemoved))
    testAuthenticatedUserButNoUserAnswers(updateRegisteredDetailsBaseUrl + normalRoutePath(indexOfWarehouseToBeRemoved))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(UpdateRegisteredDetails, updateRegisteredDetailsBaseUrl + normalRoutePath(indexOfWarehouseToBeRemoved))
  }

  s"GET " + checkRoutePath(indexOfWarehouseToBeRemoved) - {
    "when the userAnswers contains no data" - {
      "should redirect away when no data exists" in {
        build
          .commonPrecondition

        setAnswers(emptyUserAnswersForUpdateRegisteredDetails)

        WsTestClient.withClient { client =>
          val result = createClientRequestGet(client, updateRegisteredDetailsBaseUrl + checkRoutePath("indexDoesntExist"))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.WarehouseDetailsController.onPageLoad(CheckMode).url)
          }
        }
      }
    }

    userAnswersForUpdateRegisteredDetailsRemoveWarehouseDetailsPage(indexOfWarehouseToBeRemoved).foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page without the " + key + " radio checked" in {
          build
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result = createClientRequestGet(client, updateRegisteredDetailsBaseUrl + checkRoutePath(indexOfWarehouseToBeRemoved))

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(messages("updateRegisteredDetails.removeWarehouseDetails" + ".title"))
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

    testUnauthorisedUser(updateRegisteredDetailsBaseUrl + checkRoutePath(indexOfWarehouseToBeRemoved))
    testAuthenticatedUserButNoUserAnswers(updateRegisteredDetailsBaseUrl + checkRoutePath(indexOfWarehouseToBeRemoved))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(UpdateRegisteredDetails, updateRegisteredDetailsBaseUrl + checkRoutePath(indexOfWarehouseToBeRemoved))
  }

  s"POST " + normalRoutePath(indexOfWarehouseToBeRemoved) - {

    userAnswersForUpdateRegisteredDetailsRemoveWarehouseDetailsPage(indexOfWarehouseToBeRemoved).foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        "should update the session with the new value and redirect to the index controller" - {
          "when the session contains no data for page" in {
            build
              .commonPrecondition

            setAnswers(emptyUserAnswersForUpdateRegisteredDetails)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, updateRegisteredDetailsBaseUrl + normalRoutePath(indexOfWarehouseToBeRemoved), Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.WarehouseDetailsController.onPageLoad(NormalMode).url)
              }
            }
          }

          "when the session already contains data for page" in {
            build
              .commonPrecondition

            setAnswers(userAnswers)
            getAnswers(userAnswers.id).get.warehouseList.size mustBe 1
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, updateRegisteredDetailsBaseUrl + normalRoutePath(indexOfWarehouseToBeRemoved), Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.WarehouseDetailsController.onPageLoad(NormalMode).url)
                val userAnswersAfterTest = getAnswers(userAnswers.id)
                val dataStoredForPage = userAnswersAfterTest.fold[Option[Boolean]](None)(_.get(RemoveWarehouseDetailsPage))
                if(yesSelected) {
                  userAnswersAfterTest.get.warehouseList.size mustBe 0
                } else {
                  userAnswersAfterTest.get.warehouseList.size mustBe 1
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
        build
          .commonPrecondition

        setAnswers(
          emptyUserAnswersForUpdateRegisteredDetails
            .copy(warehouseList = Map(indexOfWarehouseToBeRemoved -> Site(ukAddress))))
        getAnswers(emptyUserAnswersForUpdateRegisteredDetails.id).get.warehouseList.size mustBe 1
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, updateRegisteredDetailsBaseUrl + normalRoutePath(indexOfWarehouseToBeRemoved), Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + messages("updateRegisteredDetails.removeWarehouseDetails" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe messages("updateRegisteredDetails.removeWarehouseDetails" + ".error.required")
            page.getElementsByClass("govuk-body-m").text() mustBe s"${ukAddress.lines.mkString(", ")} ${ukAddress.postCode}"
            getAnswers(emptyUserAnswersForUpdateRegisteredDetails.id).get.warehouseList.size mustBe 1
          }
        }
      }
    }
    testUnauthorisedUser(updateRegisteredDetailsBaseUrl + normalRoutePath(indexOfWarehouseToBeRemoved), Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(updateRegisteredDetailsBaseUrl + normalRoutePath(indexOfWarehouseToBeRemoved), Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(UpdateRegisteredDetails, updateRegisteredDetailsBaseUrl +
      normalRoutePath(indexOfWarehouseToBeRemoved), Some(Json.obj("value" -> "true")))
  }

  s"POST " + checkRoutePath(indexOfWarehouseToBeRemoved) - {

    userAnswersForUpdateRegisteredDetailsRemoveWarehouseDetailsPage(indexOfWarehouseToBeRemoved).foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        "should update the session with the new value and redirect to the checkAnswers controller" - {
          "when the session contains no data for page" in {
            build
              .commonPrecondition

            setAnswers(emptyUserAnswersForUpdateRegisteredDetails)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, updateRegisteredDetailsBaseUrl + checkRoutePath(indexOfWarehouseToBeRemoved), Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.WarehouseDetailsController.onPageLoad(CheckMode).url)
              }
            }
          }

          "when the session already contains data for page" in {
            build
              .commonPrecondition

            setAnswers(userAnswers)
            getAnswers(userAnswers.id).get.warehouseList.size mustBe 1
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, updateRegisteredDetailsBaseUrl + checkRoutePath(indexOfWarehouseToBeRemoved), Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.WarehouseDetailsController.onPageLoad(CheckMode).url)
                val userAnswersAfterTest = getAnswers(userAnswers.id)
                val dataStoredForPage = userAnswersAfterTest.fold[Option[Boolean]](None)(_.get(RemoveWarehouseDetailsPage))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
                if(yesSelected) {
                  userAnswersAfterTest.get.warehouseList.size mustBe 0
                } else {
                  userAnswersAfterTest.get.warehouseList.size mustBe 1
                }
              }
            }
          }
        }
      }
    }

    "when the user does not select yes or no" - {
      "should return 400 with required error" in {
        build
          .commonPrecondition

        setAnswers(
          emptyUserAnswersForUpdateRegisteredDetails
            .copy(warehouseList = Map(indexOfWarehouseToBeRemoved -> Site(ukAddress))))
        getAnswers(emptyUserAnswersForUpdateRegisteredDetails.id).get.warehouseList.size mustBe 1
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, updateRegisteredDetailsBaseUrl + checkRoutePath(indexOfWarehouseToBeRemoved), Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            getAnswers(emptyUserAnswersForUpdateRegisteredDetails.id).get.warehouseList.size mustBe 1
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + messages("updateRegisteredDetails.removeWarehouseDetails" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe messages("updateRegisteredDetails.removeWarehouseDetails" + ".error.required")
            page.getElementsByClass("govuk-body-m").text() mustBe s"${ukAddress.lines.mkString(", ")} ${ukAddress.postCode}"
          }
        }
      }
    }
    testUnauthorisedUser(updateRegisteredDetailsBaseUrl + checkRoutePath(indexOfWarehouseToBeRemoved), Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers(updateRegisteredDetailsBaseUrl + checkRoutePath(indexOfWarehouseToBeRemoved), Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType(UpdateRegisteredDetails, updateRegisteredDetailsBaseUrl +
      checkRoutePath(indexOfWarehouseToBeRemoved), Some(Json.obj("value" -> "true")))
  }
}
